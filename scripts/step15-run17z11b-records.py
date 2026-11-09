#!/usr/bin/env python3
"""Step 15 run 17z11b: removes the CombatDamageTriggerContextEffect capability interface from the effect records.

Idempotent. For every record or interface in model/effect/ that implements CombatDamageTriggerContextEffect, drops the
interface from its implements list (CardEffect when it was the only one) and deletes its combatDamageTriggerContext()
method with the comment or Javadoc above it; then deletes the interface itself. Optional repository-root argument.
"""
import os
import re
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
EFFECT_DIR = os.path.join(ROOT, 'magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect')
IFACE = 'CombatDamageTriggerContextEffect'


def drop_method(src):
    while True:
        m = re.search(r'\n([ \t]*@Override\s*\n)?[ \t]*public (?:CombatDamageTriggerContextEffect\.)?TriggerContext '
                      r'combatDamageTriggerContext\(\)\s*\{', src)
        if not m:
            return src
        start = m.start()
        # Pull in a Javadoc or line comments directly above the method.
        before = src[:start]
        jd = re.search(r'\n[ \t]*/\*\*(?:(?!\*/).)*\*/[ \t]*$', before, re.S)
        if jd:
            start = jd.start()
        else:
            lc = re.search(r'(\n[ \t]*//[^\n]*)+$', before)
            if lc:
                start = lc.start()
        depth = 0
        i = m.end() - 1
        while True:
            c = src[i]
            if c == '{':
                depth += 1
            elif c == '}':
                depth -= 1
                if depth == 0:
                    break
            i += 1
        end = i + 1
        src = src[:start] + src[end:]


def fix_implements(src):
    def repl(m):
        kw, names = m.group(1), [n.strip() for n in m.group(2).split(',')]
        names = [n for n in names if n != IFACE]
        if not names:
            names = ['CardEffect']
        return kw + ' ' + ', '.join(names) + m.group(3)
    return re.sub(r'(implements|extends)\s+([^{]*?\b' + IFACE + r'\b[^{]*?)(\s*\{)', repl, src, count=1)


changed = 0
for name in sorted(os.listdir(EFFECT_DIR)):
    path = os.path.join(EFFECT_DIR, name)
    if name == IFACE + '.java':
        continue
    with open(path) as f:
        src = f.read()
    if IFACE not in src and 'combatDamageTriggerContext()' not in src:
        continue
    new = drop_method(fix_implements(src))
    new = re.sub(r'import com\.github\.laxika\.magicalvibes\.model\.effect\.' + IFACE + r'(\.TriggerContext)?;\n', '', new)
    new = re.sub(r'\n{3,}(\s*\})', r'\n\n\1', new)
    new = re.sub(r'\n\n(\s*\}\s*)$', r'\n\1', new)
    if new != src:
        with open(path, 'w') as f:
            f.write(new)
        changed += 1
iface = os.path.join(EFFECT_DIR, IFACE + '.java')
if os.path.exists(iface):
    os.remove(iface)
    print('removed', IFACE)
print('records changed:', changed)
