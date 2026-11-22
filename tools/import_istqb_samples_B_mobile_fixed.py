#!/usr/bin/env python3
"""Import OFFICIAL ISTQB CTFL v4.0 Sample A-D PDFs into an Android app.

Works with PDF page crops rather than re-typesetting text: tables, symbols,
and diagrams stay as they are in the original ISTQB PDFs.

Install: python -m pip install pymupdf requests
Run: python tools/import_istqb_samples.py --project-root .
Optional: --local-dir path/to/downloaded/pdfs --sets A

The generated PNG assets contain copyrighted ISTQB material. Keep them for
permitted personal use; obtain appropriate permission before redistributing.
"""
from __future__ import annotations

import argparse
import json
import re
import shutil
import tempfile
from collections import Counter
from pathlib import Path

try:
    import pymupdf as fitz  # Supported PyMuPDF import; avoids deprecated fitz alias
except ImportError as exc:
    raise SystemExit('Install required dependency: python -m pip install pymupdf requests') from exc

SOURCES = {
    'A': ('1.7', '3352', '3357'),
    'B': ('1.7', '3359', '3365'),
    'C': ('1.6', '3369', '3372'),
    'D': ('1.5', '3376', '3380'),
}
BASE = 'https://istqb.org/wp-content/uploads/sdm-uploads/'


def download(letter: str, kind: str, dest: Path) -> None:
    try:
        import requests
    except ImportError as exc:
        raise SystemExit('Install required dependency: python -m pip install requests') from exc
    ver, qid, aid = SOURCES[letter]
    basename = f'ISTQB_CTFL_v4.0_Sample-Exam-{letter}-{kind}_v{ver}.pdf'
    urls = [BASE + basename, f'https://istqb.org/?download_id={qid if kind == "Questions" else aid}&sdm_process_download=1']
    last = None
    for url in urls:
        try:
            response = requests.get(url, timeout=40, headers={'User-Agent': 'Mozilla/5.0 ISTQB-Personal-Importer'})
            response.raise_for_status()
            if response.content[:4] != b'%PDF':
                raise ValueError('response is not PDF')
            dest.write_bytes(response.content)
            print(f'  downloaded {letter} {kind} from {url}')
            return
        except (requests.RequestException, ValueError) as exc:
            last = exc
    raise RuntimeError(f'Could not download {basename}: {last}. Use --local-dir to supply the official PDFs.')


def locate_pdf(local_dir: Path, letter: str, kind: str) -> Path:
    ver = SOURCES[letter][0]
    matches = list(local_dir.glob(f'*Sample-Exam-{letter}-{kind}*v{ver}*.pdf'))
    if not matches:
        matches = list(local_dir.glob(f'*Sample*{letter}*{kind}*.pdf'))
    if len(matches) != 1:
        raise RuntimeError(f'Expected one Sample {letter} {kind} PDF in {local_dir}; found {len(matches)}')
    return matches[0]


def page_lines(page):
    result = []
    for block in page.get_text('dict')['blocks']:
        if 'lines' not in block:
            continue
        for line in block['lines']:
            spans = line['spans']
            text = ''.join(s['text'] for s in spans).strip()
            if text:
                result.append((text, fitz.Rect(line['bbox'])))
    return result


def question_starts(doc):
    """Find Q1–Q40 in the real Questions section, not in the table of contents.

    ISTQB's PDF lists e.g. "Question #1 (1 Point)" on both its TOC page
    and the actual exam page. Looking through every page without checking
    the section causes "Duplicate question heading 1".
    """
    patt = re.compile(r'^Question\s*#\s*(\d{1,2})\s*\(\s*1\s+Point', re.I)
    section_page = None
    for page_no, page in enumerate(doc):
        # In the TOC the line contains dots and a page number, whereas the
        # actual Questions section has a standalone heading near the top.
        for text, rect in page_lines(page):
            if text.strip().lower() == 'questions' and rect.y0 < 250:
                section_page = page_no
                break
        if section_page is not None:
            break
    if section_page is None:
        raise RuntimeError('Cannot find the main Questions section in this PDF')

    found = {}
    for page_no in range(section_page, len(doc)):
        for text, rect in page_lines(doc[page_no]):
            m = patt.match(text)
            if not m:
                continue
            n = int(m.group(1))
            if 1 <= n <= 40:
                if n in found:
                    raise RuntimeError(
                        f'Duplicate real question heading {n} on PDF pages '
                        f'{found[n][0]+1} and {page_no+1}'
                    )
                found[n] = (page_no, max(0., rect.y0 - 3))
    missing = sorted(set(range(1, 41)) - set(found))
    if missing:
        raise RuntimeError(f'Question headings not complete; missing {missing}')
    if list(found.values()) != sorted(found.values()):
        raise RuntimeError('Questions are not in ascending document order; inspect PDF manually')
    print(f'  located all 40 real question headings, starting PDF page {section_page+1}')
    return found


def parse_answer_key(doc):
    """Read *table* rows from their visual x/y positions, not PDF text order.

    Supports the official two-column Answer Key layouts (and one-column keys).
    Each LO word anchors a row; number, correct letter(s) and K level must
    appear on the same horizontal line for that table half.
    """
    found = {}
    for page_idx, page in enumerate(doc):
        text = page.get_text('text')
        # Only the explicitly labeled answer key, before the rationale section.
        if 'Answer Key' not in text or 'FL-' not in text:
            continue
        words = page.get_text('words')
        for w in words:
            token = w[4].strip()
            if not re.fullmatch(r'FL-\d+\.\d+\.\d+', token):
                continue
            cy = (w[1] + w[3]) / 2
            # Key row may have single-letter words, comma tokens or 'a, e'.
            same_row = [z for z in words if abs(((z[1] + z[3]) / 2) - cy) < 4.0]
            before = sorted((z for z in same_row if w[0]-180 < z[0] < w[0]-5), key=lambda z: z[0])
            nums = [int(z[4]) for z in before if re.fullmatch(r'\d{1,2}', z[4])]
            if not nums:
                continue
            number = nums[-1]
            if not 1 <= number <= 40:
                continue
            # between the number and LO are correct letters, and perhaps a comma.
            number_word = next(z for z in before if z[4] == str(number))
            answer_words = [z[4].strip().lower() for z in before if z[0] > number_word[0] + 5]
            ans = re.findall(r'[a-e]', ' '.join(answer_words))
            if not ans:
                continue
            after = sorted((z for z in same_row if w[2]+2 < z[0] < w[2]+100), key=lambda z: z[0])
            k = next((z[4] for z in after if re.fullmatch(r'K[123]', z[4])), None)
            if not k:
                continue
            payload = {'correct': sorted(set('abcde'.index(a) for a in ans)), 'lo': token, 'kLevel': k}
            if number in found and found[number] != payload:
                raise RuntimeError(f'Conflicting answer-key rows for question {number}')
            found[number] = payload
    if set(found) != set(range(1, 41)):
        raise RuntimeError('Could not reliably read all 40 answer-key rows. Missing: '+str(sorted(set(range(1, 41))-set(found))))
    return found


def answer_rationale_starts(doc):
    """Locate the 40 rationale rows by their leftmost number cells."""
    starts = {}
    section_started = False
    for pn, page in enumerate(doc):
        lines = page_lines(page)
        if any(txt.strip() == 'Answers' and rect.y0 < 180 for txt,rect in lines):
            section_started = True
        if not section_started:
            continue
        for txt, rect in lines:
            if not re.fullmatch(r'(?:[1-9]|[1-3]\d|40)', txt):
                continue
            # Some official B rationales (notably #3 and #23) start at the
            # very top of a page without the repeated table-column header.
            # Their number cells may be at y≈65–100, so 105 would discard them.
            # Keep the x restriction to the leftmost number column and skip
            # the actual page heading/footer using these conservative bounds.
            if rect.x0 > 125 or rect.y0 < 50 or rect.y0 > page.rect.height-60:
                continue
            n = int(txt)
            # Ignore point-value digits in the last column.
            if n not in starts:
                starts[n] = (pn, max(48., rect.y0-3))
        if len(starts) >= 40:
            break
    if set(starts) != set(range(1,41)):
        raise RuntimeError('Unable to locate all 40 answer rationale rows; missing '+str(sorted(set(range(1,41))-set(starts))))
    if list(starts.values()) != sorted(starts.values()):
        raise RuntimeError('Answer rationale order ambiguous; review this PDF before import')
    return starts


def extract_option_count(doc, starts, n, key):
    start = starts[n]
    end = starts.get(n + 1)
    parts = []
    for page_no in range(start[0], (end[0] if end else start[0]) + 1):
        page = doc[page_no]
        y0 = start[1] if page_no == start[0] else 60
        y1 = end[1] if end and page_no == end[0] else page.rect.height-50
        if y1 > y0:
            parts.append(page.get_text('text', clip=fitz.Rect(0,y0,page.rect.width,y1), sort=True))
    full = '\n'.join(parts)
    # Five-choice questions in official samples typically have TWO correct options.
    count = 5 if 4 in key['correct'] else 4
    if re.search(r'(?m)^\s*e\)\s+', full):
        count = 5
    return count



# Extract only straightforward, text-only questions. The original PNG remains
# the source of truth for tables, diagrams and anything ambiguous.
VISUAL_CLUES = re.compile(
    r'\b(?:table|diagram|figure|flowchart|graph|picture|illustration|chart)\b'
    r'|\bexecution\s+1\s+execution\s+2\b', re.I
)
OPTION_LINE = re.compile(r'^([a-e])\)\s*(.*)$')
HEADING_LINE = re.compile(r'^Question\s*#\s*\d{1,2}\s*\(\s*1\s+Point', re.I)
INSTRUCTION_LINE = re.compile(r'^Select\s+(ONE|TWO|THREE)\s+options?\.?$', re.I)


def _region_for_question(doc, starts, n):
    first, top = starts[n]
    last, bottom = starts.get(n+1, (None, None))
    if last is None:
        # End of the main exam (never include the additional questions appendix).
        last = first
        for i in range(first, min(len(doc), first+3)):
            stops = [r.y0 for t, r in page_lines(doc[i])
                     if re.match(r'^(?:Appendix:|Additional Questions|Question\s*#\s*A1\b)', t, re.I)]
            if stops:
                last, bottom = i, min(stops)-5
                break
    for pn in range(first, last+1):
        page = doc[pn]
        y0 = top if pn == first else 82.0
        y1 = bottom-2 if (pn == last and bottom is not None) else page.rect.height-52
        if y1 > y0:
            yield page, fitz.Rect(39, y0, page.rect.width-36, y1)


def _has_embedded_visuals(page, region):
    # A question with graphics is safer to show from the original PDF crop.
    for entry in page.get_image_info():
        r = fitz.Rect(entry['bbox']) & region
        if not r.is_empty and r.get_area() > 60:
            return True
    for d in page.get_drawings():
        r = fitz.Rect(d['rect']) & region
        if not r.is_empty and r.get_area() > 50:
            return True
    return False


def extract_mobile_question(doc, starts, n, expected_count, correct_indices,
                            allow_minor_pdf_drawings=False, exclude_graphics=None,
                            allow_visual_keywords=False):
    """Return mobile-native question/options, or None (safe PDF fallback).

    Only auto-enable text when headings, option labels, answer count, and
    expected selection instruction are all coherent. Never guess missing text.
    """
    page_lines_all = []
    contains_visual = False
    for page, region in _region_for_question(doc, starts, n):
        contains_visual |= _has_embedded_visuals(page, region)
        # PDF often stores 'a)' and its answer in separate spans at the same y.
        # Rejoin them by baseline and horizontal position before parsing.
        lines = [(text, rect) for text, rect in page_lines(page)
                 if rect.y0 >= region.y0 - 2 and rect.y1 <= region.y1 + 3
                 and rect.x1 > region.x0 and rect.x0 < region.x1
                 and not (exclude_graphics and page.number == exclude_graphics[0]
                          and rect.y0 >= exclude_graphics[1].y0
                          and rect.y1 <= exclude_graphics[1].y1)]
        lines.sort(key=lambda z: ((z[1].y0+z[1].y1)/2, z[1].x0))
        grouped = []
        for text, rect in lines:
            cy = (rect.y0+rect.y1)/2
            if grouped and abs(grouped[-1][0]-cy) <= 3.5:
                grouped[-1][1].append((rect.x0, text))
            else:
                grouped.append((cy, [(rect.x0, text)]))
        page_lines_all.extend(' '.join(t.strip() for _, t in sorted(parts)).strip()
                              for _, parts in grouped)

    # Throw away known page furniture. Do not silently skip any body lines.
    lines = [x.strip() for x in page_lines_all if x.strip()]
    while lines and not HEADING_LINE.match(lines[0]):
        lines.pop(0)
    if not lines or not HEADING_LINE.match(lines[0]):
        return None
    lines.pop(0)
    body, choices = [], {}
    current = None
    instruction = None
    for line in lines:
        if INSTRUCTION_LINE.match(line):
            instruction = INSTRUCTION_LINE.match(line).group(1).upper()
            break
        match = OPTION_LINE.match(line)
        if match:
            letter, content = match.groups()
            if letter in choices or (ord(letter)-ord('a')) != len(choices):
                return None
            current = letter
            choices[letter] = [content] if content else []
        elif current is None:
            body.append(line)
        else:
            choices[current].append(line)
    if instruction is None or (contains_visual and not allow_minor_pdf_drawings):
        return None
    if len(choices) != expected_count:
        return None
    if sorted(choices) != list('abcde'[:expected_count]):
        return None
    required = {'ONE': 1, 'TWO': 2, 'THREE': 3}.get(instruction)
    if required != len(correct_indices):
        return None
    if not body or (exclude_graphics is None and not allow_visual_keywords
                    and VISUAL_CLUES.search(' '.join(body))):
        return None

    def combine(rows):
        # Keep list items as separate lines; reflow normal PDF line wrapping.
        pieces = []
        for line in rows:
            if re.match(r'^(?:[•\-]|[ivx]+\.|\d+[.)])\s+', line, re.I):
                pieces.append('\n'+line)
            else:
                pieces.append((' ' if pieces else '')+line)
        return ''.join(pieces).strip()

    question = combine(body)
    options = [combine(choices[k]) for k in 'abcde'[:expected_count]]
    if len(question) < 12 or any(not x for x in options):
        return None
    if any('Question #' in x for x in [question]+options):
        return None
    return {'questionText': question, 'optionTexts': options,
            'presentation': 'text'}


# Sample A v1.7: identified figures only. Never guess table positions from
# an unrelated version or from an unverified exam. Every failed check falls
# back to the original PDF crop rather than silently losing exam content.
HYBRID_SAMPLE_A = {
    14: (r"Execution\s*1|Execution\s*2", r"Tests\s+are\s+repeated"),
    21: (r"Final\s+result", r"What\s+is\s+the\s+2.value"),
    22: (r"Conditions|Being\s+a\s+member", r"Based\s+ONLY"),
    23: (r"OFF\s+state|The\s+system\s+starts", r"What\s+is\s+the\s+MINIMAL"),
    33: (r"Test\s+case\s+number|Test\s+condition\s+covered|TC\s*001", r"Which\s+of\s+the\s+following\s+test\s+cases"),
}


def find_q21_table_area(doc, starts):
    """Sample A v1.7 question 21: locate its table by surrounding prose.

    The six TC rows may be grouped in different ways by PDF extractors.
    Instead, find the line immediately ABOVE the table and the question line
    immediately BELOW it. Both are present in the official PDF. Verify that
    the bounded region contains the expected table data before cropping.
    """
    for page, region in _region_for_question(doc, starts, 21):
        lines = [(text.strip(), rect) for text, rect in page_lines(page)
                 if rect.y1 >= region.y0 and rect.y0 <= region.y1]
        before = [r for t, r in lines if re.search(
            r'following\s+set\s+of\s+test\s+cases|you\s+have\s+prepared', t, re.I)]
        after = [r for t, r in lines if re.search(
            r'what\s+is\s+the\s+2.value|what\s+is\s+the', t, re.I)]
        if not before or not after:
            print('  Q21 diagnostic: boundary sentences not found on this PDF page')
            continue
        top = max(r.y1 for r in before) + 4
        below = [r.y0 for r in after if r.y0 > top]
        if not below:
            print('  Q21 diagnostic: no question sentence after table')
            continue
        bottom = min(below) - 5
        if not 45 < bottom - top < 260:
            print(f'  Q21 diagnostic: unexpected figure height {bottom-top:.1f}')
            continue
        band = fitz.Rect(region.x0, top, region.x1, bottom)
        raw = page.get_text('text', clip=band, sort=True)
        flat = re.sub(r'\s+', '', raw).lower()
        # Validate the two ends and essential table contents. Do not publish
        # an image unless the authentic table data can be seen in this region.
        if not all(token in flat for token in ('tc1', 'tc6', '91', '50', '80')):
            print('  Q21 diagnostic: bounded region did not contain expected TC table')
            continue
        words = [fitz.Rect(w[:4]) for w in page.get_text('words')
                 if w[1] >= top - 3 and w[3] <= bottom + 3]
        if not words:
            continue
        left = max(region.x0, min(r.x0 for r in words) - 10)
        right = min(region.x1, max(r.x1 for r in words) + 10)
        # Expand to vector table borders where present, without adding the
        # large page margins or next question's text.
        for drawing in page.get_drawings():
            dr = fitz.Rect(drawing['rect'])
            if dr.y1 >= top and dr.y0 <= bottom and 20 < dr.width < 480:
                left = max(region.x0, min(left, dr.x0 - 3))
                right = min(region.x1, max(right, dr.x1 + 3))
        result = fitz.Rect(left, top, right, bottom) & region
        if result.is_empty or result.width < 100 or result.height < 45:
            continue
        print('  Q21: table located between surrounding paragraphs; compact crop ready')
        return page.number, result
    return None


def find_figure_area(doc, starts, number):
    """Return (page number, content rectangle) for a verified A table/diagram.

    Use the text immediately above/below the figure, so the rendered asset
    cannot include the whole PDF page, answer choices, or page footer.
    """
    if number not in HYBRID_SAMPLE_A:
        return None
    if number == 21:
        return find_q21_table_area(doc, starts)
    begin_re, end_re = (re.compile(s, re.I) for s in HYBRID_SAMPLE_A[number])
    for page, region in _region_for_question(doc, starts, number):
        lines = sorted(((txt, r) for txt, r in page_lines(page)
                        if region.y0 - 3 <= r.y0 and r.y1 <= region.y1 + 3),
                       key=lambda it: (it[1].y0, it[1].x0))
        begins = [r for t, r in lines if begin_re.search(t)]
        ends = [r for t, r in lines if end_re.search(t)]
        if not begins or not ends:
            continue
        # For diagram 23 the starting anchor is the final sentence of the
        # prompt (above the figure); for tables it is their first header row.
        start = (max(r.y1 for r in begins) + 4 if number == 23
                 else max(r.y0 for r in begins) - 9 if number == 21
                 else min(r.y0 for r in begins) - 9)
        stop = min(r.y0 for r in ends) - 7
        if not (25 < stop - start < 320):
            continue
        # Calculate a tight x-range from figure words and vector paths.
        words = [w for w in page.get_text('words')
                 if w[1] >= start and w[3] <= stop]
        graphics = [fitz.Rect(g['rect']) for g in page.get_drawings()
                    if g['rect'][3] >= start and g['rect'][1] <= stop]
        xs = [w[0] for w in words] + [w[2] for w in words]
        xs += [r.x0 for r in graphics if r.width > 12]
        xs += [r.x1 for r in graphics if r.width > 12]
        left = max(region.x0, min(xs) - 12) if xs else region.x0
        right = min(region.x1, max(xs) + 12) if xs else region.x1
        if right - left < 120:
            continue
        clip = fitz.Rect(left, start, right, stop) & region
        if clip.is_empty:
            continue
        return page.number, clip
    return None



# Confirmed against the official CTFL Sample Exam B v1.7 Questions PDF.
# These boundaries enclose just the original table/diagram/log, NOT its
# answer choices or the surrounding PDF header/footer. B19 needs no figure.
# The fail-closed checks below keep the complete question crop if an anchor
# or a figure's expected content cannot be reliably found.
HYBRID_SAMPLE_B = {
    22: {
        'before': r'atherosclerosis',
        'after': r'What\s+is\s+the\s+decision\s+table\s+coverage',
        'tokens': ('rule1', 'rule5', 'tc1', 'tc5', 'cholesterol'),
    },
    23: {
        'before': r'currently\s+stored\s+elements',
        'after': r'Which\s+of\s+the\s+following\s+test\s+cases',
        'tokens': ('start', 'full', 'add', 'remove'),
    },
    31: {
        'before': r'historical\s+data',
        'after': r'The\s+estimated\s+development\s+effort',
        'tokens': ('p1', 'p4', '800,000', '120,000'),
    },
    32: {
        'before': r'based\s+on\s+test\s+priority',
        'after': r'You\s+also\s+identified\s+the\s+following',
        'tokens': ('tc1', 'tc7', 'priority'),
    },
    38: {
        'before': r'log\s+from\s+the\s+test\s+execution\s+looks\s+as\s+follows',
        'after': r'Which\s+of\s+the\s+following\s+provides',
        'tokens': ('tc1', 'tc5', 'result', 'failed'),
    },
}


def find_sample_b_figure_area(doc, starts, number):
    """Locate a verified B v1.7 visual between its surrounding paragraphs.

    Never guess coordinates from a different sample set or PDF version.
    Check anchor order, image dimensions, and real figure content before use.
    """
    spec = HYBRID_SAMPLE_B.get(number)
    if spec is None:
        return None
    for page, region in _region_for_question(doc, starts, number):
        lines = [(t.strip(), r) for t, r in page_lines(page)
                 if r.y0 >= region.y0 - 3 and r.y1 <= region.y1 + 3]
        before = [r for t, r in lines if re.search(spec['before'], t, re.I)]
        after = [r for t, r in lines if re.search(spec['after'], t, re.I)]
        if not before or not after:
            print(f'  B Q{number}: figure boundary text not found on PDF page {page.number+1}')
            continue
        # The LAST before-anchor and FIRST subsequent after-anchor bound the
        # figure (possibly including its associated test-data list, B22).
        top = max(r.y1 for r in before) + 3
        later = [r.y0 for r in after if r.y0 > top]
        if not later:
            continue
        bottom = min(later) - 4
        if not 38 < bottom - top < 450:
            print(f'  B Q{number}: unexpected figure height {bottom - top:.1f}')
            continue
        band = fitz.Rect(region.x0, top, region.x1, bottom) & region
        flattened = re.sub(r'\s+', '', page.get_text('text', clip=band, sort=True)).lower()
        missing = [t for t in spec['tokens'] if t not in flattened]
        if missing:
            # In B23 the original state diagram may be embedded as a vector
            # drawing or image rather than searchable text. Its position is
            # still bounded by two exact question sentences; verify substantial
            # graphic geometry before accepting a text-free diagram.
            graphic_count = sum(
                1 for drawing in page.get_drawings()
                if ((fitz.Rect(drawing['rect']) & band).get_area() > 150)
            )
            large_image = any(
                (fitz.Rect(img['bbox']) & band).get_area() > 2500
                for img in page.get_image_info()
            )
            if not (number == 23 and (graphic_count >= 4 or large_image)):
                print(f'  B Q{number}: figure content validation failed: {missing}')
                continue

        # Tighten horizontal crop around words and vector shapes; preserve
        # full table borders/diagram arrows, avoiding the PDF page margins.
        components = []
        for w in page.get_text('words'):
            r = fitz.Rect(w[:4])
            if r.y0 >= top - 2 and r.y1 <= bottom + 2:
                components.append(r)
        for draw in page.get_drawings():
            r = fitz.Rect(draw['rect'])
            if (r.y1 >= top and r.y0 <= bottom and r.width > 20
                    and r.height < 460):
                components.append(r)
        for entry in page.get_image_info():
            r = fitz.Rect(entry['bbox'])
            if r.y1 >= top and r.y0 <= bottom and r.width > 20:
                components.append(r)
        if not components:
            continue
        left = max(region.x0, min(r.x0 for r in components) - 8)
        right = min(region.x1, max(r.x1 for r in components) + 8)
        clip = fitz.Rect(left, top, right, bottom) & region
        if clip.is_empty or clip.width < 135 or clip.height < 40:
            continue
        print(f'  B Q{number}: verified isolated figure on PDF page {page.number+1}')
        return page.number, clip
    return None

def render_figure(doc, figure, path):
    pn, rect = figure
    pix = doc[pn].get_pixmap(matrix=fitz.Matrix(2.3, 2.3), clip=rect,
                             alpha=False)
    pix.save(str(path))
    if path.stat().st_size < 1000:
        raise RuntimeError(f'Figure rendered too small: {path}')


def render_crop(doc, starts, n, out_path, top_margin=80, bottom_margin=52, resolution=1.7):
    """Crop question/rationale across page boundaries, preserving diagrams."""
    first_page, top = starts[n]
    if n < 40:
        last_page, bottom = starts[n+1]
    else:
        last_page, bottom = first_page, None
        # Q40 typically finishes on its own or the next page; avoid additional Q A1.
        for i in range(first_page, min(first_page + 2, len(doc))):
            pg = doc[i]
            endings = [r.y0 for t,r in page_lines(pg) if re.match(r'^(?:Appendix:|Question #A1\b|Additional Questions)',t, re.I)]
            if endings:
                last_page, bottom = i, min(endings)-8
                break
        if bottom is None:
            last_page, bottom = first_page, None
    images = []
    from PIL import Image as PILImage
    for i in range(first_page, last_page+1):
        page = doc[i]
        y0 = top if i == first_page else top_margin
        y1 = bottom-4 if i == last_page and bottom is not None else page.rect.height-bottom_margin
        if y1 <= y0+8:
            continue
        clip = fitz.Rect(38, y0, page.rect.width-35, y1)
        pix = page.get_pixmap(matrix=fitz.Matrix(resolution,resolution),clip=clip, alpha=False)
        img = PILImage.frombytes('RGB', [pix.width,pix.height], pix.samples)
        images.append(img)
    if not images:
        raise RuntimeError(f'Empty crop for question {n}')
    width = max(im.width for im in images)
    total_height = sum(im.height for im in images) + (len(images)-1)*8
    output = PILImage.new('RGB',(width,total_height),'white')
    y = 0
    for im in images:
        output.paste(im,(0,y))
        y += im.height+8
    output.save(out_path,optimize=True)
    if out_path.stat().st_size < 1200:
        raise RuntimeError(f'Suspiciously small crop for question {n}: {out_path}')


def import_one(letter, root, local_dir, cache_dir):
    ver = SOURCES[letter][0]
    if local_dir is not None:
        qpath = locate_pdf(local_dir,letter,'Questions')
        apath = locate_pdf(local_dir,letter,'Answers')
    else:
        qpath = cache_dir/f'{letter}_questions.pdf'
        apath = cache_dir/f'{letter}_answers.pdf'
        download(letter,'Questions',qpath)
        download(letter,'Answers',apath)
    # Load PDF bytes into memory. On Windows, fitz.open(path) can keep the
    # downloaded file locked while TemporaryDirectory removes the cache,
    # masking the actual parsing error with PermissionError [WinError 32].
    # Opening from bytes avoids OS-level file locks on cached PDFs.
    qdoc = fitz.open(stream=qpath.read_bytes(), filetype='pdf')
    try:
        adoc = fitz.open(stream=apath.read_bytes(), filetype='pdf')
    except Exception:
        qdoc.close()
        raise
    # Parsing can fail on differently laid-out PDF versions. Close the
    # documents before TemporaryDirectory tries to delete them on Windows;
    # otherwise WinError 32 masks the original parsing error.
    try:
        starts = question_starts(qdoc)
        keys = parse_answer_key(adoc)
        explanations = answer_rationale_starts(adoc)
    except Exception:
        qdoc.close()
        adoc.close()
        raise
    # Generate in a temp directory; publish only when all 40 crops validated.
    dest = root/'app/src/main/assets/official_samples'/letter
    dest.parent.mkdir(parents=True,exist_ok=True)
    temp = Path(tempfile.mkdtemp(prefix=f'istqb_{letter}_',dir=dest.parent))
    try:
        manifest = {'letter':letter,'version':ver,'source':'ISTQB CTFL v4.0.1 sample exam', 'questions':[]}
        native_count = 0
        for i in range(1,41):
            qimg = f'official_samples/{letter}/q{i:02}.png'
            aimg = f'official_samples/{letter}/a{i:02}.png'
            render_crop(qdoc,starts,i,temp/f'q{i:02}.png')
            render_crop(adoc,explanations,i,temp/f'a{i:02}.png', top_margin=100)
            meta=keys[i]
            count=extract_option_count(qdoc,starts,i,meta)
            if max(meta['correct']) >= count:
                raise RuntimeError(f'Answer key for question {i} exceeds option count')
            chapter=int(meta['lo'].split('.')[0].split('-')[1])
            row = {
                'number':i,'optionCount':count,'correct':meta['correct'],
                'questionImage':qimg,'answerImage':aimg,
                'lo':meta['lo'],'kLevel':meta['kLevel'],'chapter':chapter
            }
            # Four confirmed text-only questions in official Sample A v1.7 can
            # contain harmless PDF vector decorations. Those must not be
            # mistaken for a table or diagram. Keep every other question on
            # strict visual fallback until it is explicitly inspected.
            text_only_verified = (
                (letter == 'A' and ver == '1.7' and i in {4, 10, 18, 20}) or
                (letter == 'B' and ver == '1.7' and i == 19)
            )
            parsed = extract_mobile_question(
                qdoc, starts, i, count, meta['correct'],
                allow_minor_pdf_drawings=text_only_verified,
                # B19 talks *about* decision table testing; the word "table"
                # does not mean the question contains a visual table.
                allow_visual_keywords=(letter == 'B' and ver == '1.7' and i == 19)
            )
            # When a question includes a table or diagram, preserve ONLY
            # that compact visual while presenting its text and choices in
            # native mobile controls. Limit this to the inspected Sample A
            # items; all other PDFs still use strict fallback.
            if parsed is None and letter == 'A' and ver == '1.7' and i in HYBRID_SAMPLE_A:
                figure = find_figure_area(qdoc, starts, i)
                if figure is not None:
                    hybrid = extract_mobile_question(
                        qdoc, starts, i, count, meta['correct'],
                        allow_minor_pdf_drawings=True, exclude_graphics=figure
                    )
                    if hybrid is not None:
                        figure_file = f'figure{i:02}.png'
                        render_figure(qdoc, figure, temp/figure_file)
                        row['originalQuestionImage'] = qimg
                        row['questionImage'] = f'official_samples/{letter}/{figure_file}'
                        hybrid['presentation'] = 'hybrid'
                        parsed = hybrid
                    else:
                        print(f'  NOTE: A question {i} hybrid text parsing failed; keeping safe PDF crop')
                else:
                    print(f'  NOTE: A question {i} figure area not located; keeping safe PDF crop')
            # B v1.7: five visually verified table/diagram/log questions.
            # As with A, retain original PNGs for transparency and fallback.
            if (parsed is None and letter == 'B' and ver == '1.7'
                    and i in HYBRID_SAMPLE_B):
                figure = find_sample_b_figure_area(qdoc, starts, i)
                if figure is not None:
                    hybrid = extract_mobile_question(
                        qdoc, starts, i, count, meta['correct'],
                        allow_minor_pdf_drawings=True,
                        exclude_graphics=figure
                    )
                    if hybrid is not None:
                        figure_file = f'figure{i:02}.png'
                        render_figure(qdoc, figure, temp/figure_file)
                        row['originalQuestionImage'] = qimg
                        row['questionImage'] = f'official_samples/{letter}/{figure_file}'
                        hybrid['presentation'] = 'hybrid'
                        parsed = hybrid
                    else:
                        print(f'  NOTE: B Q{i} text/choices extraction failed; retaining complete PDF image')
                else:
                    print(f'  NOTE: B Q{i} figure not verified; retaining complete PDF image')
            if parsed is not None:
                row.update(parsed)
                native_count += 1
            else:
                row['presentation'] = 'pdf'
            manifest['questions'].append(row)
        (temp/'manifest.json').write_text(json.dumps(manifest,indent=2,ensure_ascii=False),encoding='utf-8')
        report = dict(Counter(q['kLevel'] for q in manifest['questions']))
        if report != {'K1':8,'K2':24,'K3':8}:
            print(f'  NOTE: K-level distribution {report} differs from expected; confirm this version.')
        if dest.exists():
            shutil.rmtree(dest)
        temp.rename(dest)
        print(f'  OK Sample {letter}: 40 question crops + 40 answer rationale crops')
        print(f'  Mobile-native (text + hybrid): {native_count}/40, original-PDF fallback: {40-native_count}/40')
        print('  Remaining PDF questions:', [q['number'] for q in manifest['questions'] if q['presentation'] == 'pdf'])
        print('  Detailed A fixes:', [(n, manifest['questions'][n-1]['presentation']) for n in (14,20,21,22,23,33)] if letter == 'A' else 'n/a')
        if letter == 'B':
            print('  Detailed B fixes:', [(n, manifest['questions'][n-1]['presentation']) for n in (19,22,23,31,32,38)])
        print('  Inspect manifest.json question 1 and check it against the official PDF.')
    except Exception:
        shutil.rmtree(temp,ignore_errors=True)
        raise
    finally:
        qdoc.close(); adoc.close()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--project-root',type=Path,required=True)
    parser.add_argument('--local-dir',type=Path,help='Directory containing downloaded official PDF files; skips downloading')
    parser.add_argument('--sets',nargs='+',choices=list(SOURCES),default=list(SOURCES))
    args=parser.parse_args()
    root=args.project_root.resolve()
    if not (root/'app/src/main').exists():
        raise SystemExit('Project root must contain app/src/main')
    with tempfile.TemporaryDirectory() as d:
        for letter in args.sets:
            print(f'Importing official sample {letter}...')
            import_one(letter,root,args.local_dir,Path(d))
    print('All selected sample PDFs imported. Rebuild the Android app.')

if __name__=='__main__':
    main()
