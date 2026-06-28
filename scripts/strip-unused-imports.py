#!/usr/bin/env python3
"""Remove unused single-type/static imports from Java files.

Safe by construction: an import is only removed when its simple name does not
appear (as a whole word) anywhere in the file OUTSIDE of import/package lines.
If the name appears anywhere in real code or comments, the import is kept.
Wildcard imports (ending in .*) are never touched.
"""
import re
import sys
from pathlib import Path

IMPORT_RE = re.compile(r'^\s*import\s+(?:static\s+)?([\w.]+)\s*;\s*$')

def process(path: Path, apply: bool):
    lines = path.read_text(encoding='utf-8').splitlines(keepends=True)

    # Body = everything that is not an import or package declaration.
    body = ''.join(
        ln for ln in lines
        if not ln.lstrip().startswith('import ')
        and not ln.lstrip().startswith('package ')
    )

    removed = []
    kept = []
    for ln in lines:
        m = IMPORT_RE.match(ln)
        if m:
            fqn = m.group(1)
            if fqn.endswith('.*'):           # never touch wildcards
                kept.append(ln)
                continue
            simple = fqn.rsplit('.', 1)[-1]
            if re.search(r'\b' + re.escape(simple) + r'\b', body):
                kept.append(ln)
            else:
                removed.append(fqn)
                continue                      # drop the line
        else:
            kept.append(ln)

    if removed and apply:
        path.write_text(''.join(kept), encoding='utf-8')
    return removed

def main():
    apply = '--apply' in sys.argv
    dirs = [a for a in sys.argv[1:] if not a.startswith('--')]
    total_files = 0
    total_removed = 0
    for d in dirs:
        for f in sorted(Path(d).rglob('*.java')):
            removed = process(f, apply)
            if removed:
                total_files += 1
                total_removed += len(removed)
                print(f"{f.name}: {len(removed)} unused")
                for r in removed:
                    print(f"    {r}")
    verb = "Removed" if apply else "Would remove"
    print(f"\n{verb} {total_removed} imports across {total_files} files.")

if __name__ == '__main__':
    main()
