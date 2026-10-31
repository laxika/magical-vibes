"""Lists every CostEffect subtype in the domain module (records and interfaces), one per line.

Shared by the step 5 conversion scripts of effect-slot-refactor.md, which route a SPELL-slot
registration of one of these types to Card.addCastingCost.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
EFFECT_DIR = ROOT / 'magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect'
DECL = re.compile(r'public\s+(?:sealed\s+|non-sealed\s+)?(?:record|interface|final\s+class|class)\s+(\w+)[^{]*?'
                  r'(?:implements|extends)\s+([^{]+)\{', re.S)


def cost_types():
    parents = {}
    for path in EFFECT_DIR.glob('*.java'):
        src = path.read_text(encoding='utf-8', errors='replace')
        m = DECL.search(src)
        if m:
            supers = re.findall(r'\b([A-Z]\w*)\b', re.sub(r'<[^>]*>', '', m.group(2)))
            parents[m.group(1)] = supers
    costs = {'CostEffect'}
    changed = True
    while changed:
        changed = False
        for name, supers in parents.items():
            if name not in costs and any(s in costs for s in supers):
                costs.add(name)
                changed = True
    return costs


if __name__ == '__main__':
    for name in sorted(cost_types()):
        print(name)
    sys.exit(0)
