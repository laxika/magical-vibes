"""Helpers for the step 12a engine edits: find and remove a Java method, with its Javadoc, by its signature."""
import re


def method_span(src, signature):
    """(start, end) of the method whose declaration starts with `signature` (after indentation), Javadoc and
    annotations included; end is past the closing brace and its newline."""
    m = re.search(r"^[ \t]*" + re.escape(signature), src, re.M)
    if m is None:
        raise ValueError("no method " + signature)
    brace = src.index("{", m.end())
    depth, i, in_str, in_chr = 0, brace, False, False
    while True:
        c = src[i]
        if in_str:
            if c == "\\":
                i += 2
                continue
            if c == '"':
                in_str = False
        elif in_chr:
            if c == "\\":
                i += 2
                continue
            if c == "'":
                in_chr = False
        elif src.startswith("//", i):
            i = src.index("\n", i)
            continue
        elif src.startswith("/*", i):
            i = src.index("*/", i) + 2
            continue
        elif c == '"':
            in_str = True
        elif c == "'":
            in_chr = True
        elif c == "{":
            depth += 1
        elif c == "}":
            depth -= 1
            if depth == 0:
                break
        i += 1
    end = i + 1
    if src[end:end + 1] == "\n":
        end += 1
    start = m.start()
    while True:
        prev_end = start - 1
        prev_start = src.rfind("\n", 0, prev_end) + 1
        line = src[prev_start:prev_end].strip()
        if line.startswith(("*", "/**", "@", "//")) and line:
            start = prev_start
            continue
        annotation = _annotation_start(src, prev_start)
        if annotation is not None:
            start = annotation
            continue
        if line == "" and src[src.rfind("\n", 0, prev_start - 1) + 1:prev_start - 1].strip().endswith("*/"):
            break
        break
    # drop one blank line before the method if there is one, so removals don't leave double blanks
    if src[start - 2:start] == "\n\n":
        start -= 1
    return start, end


def _annotation_start(src, line_start):
    """Start of the multi-line annotation whose last line begins at `line_start`, or None: the nearest line above
    that starts with `@`, when every line from it down to here is non-blank and its parentheses balance here."""
    end = src.find("\n", line_start)
    lines = []
    start = line_start
    for _ in range(6):
        text = src[start:end].strip()
        if not text:
            return None
        lines.insert(0, text)
        if text.startswith("@"):
            joined = " ".join(lines)
            return start if joined.count("(") == joined.count(")") and len(lines) > 1 else None
        if start == 0:
            return None
        end = start - 1
        start = src.rfind("\n", 0, end) + 1
    return None


def remove_methods(src, *signatures):
    for signature in signatures:
        while re.search(r"^[ \t]*" + re.escape(signature), src, re.M):
            start, end = method_span(src, signature)
            src = src[:start] + src[end:]
    return src
