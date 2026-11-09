#!/usr/bin/env python3
"""Step 15, run 17z11c: drop CombatOpponentReferencingEffect from the effect records and delete the interface.

Idempotent; optional repository-root argument. Removes the interface from every `implements` list (a record that
implemented only it implements CardEffect), every `referencesCombatOpponent()` override with its @Override and the
MayEffect helper, and the unused import, then deletes the interface. Javadoc that named the interface is reworded by
hand afterwards.
"""
import pathlib
import re
import sys

root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else '.')
effect_dir = root / 'magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect'
iface = effect_dir / 'CombatOpponentReferencingEffect.java'

override = re.compile(
    r'\n(?:[ \t]*\n)?[ \t]*@Override\n[ \t]*public boolean referencesCombatOpponent\(\) \{\n.*?\n    \}\n', re.S)
helper = re.compile(
    r'\n(?:[ \t]*\n)?[ \t]*private static boolean referencesCombatOpponent\(CardEffect effect\) \{\n.*?\n    \}\n', re.S)

changed = 0
for path in sorted(effect_dir.glob('*.java')):
    if path == iface:
        continue
    text = path.read_text()
    if 'CombatOpponentReferencingEffect' not in text and 'referencesCombatOpponent' not in text:
        continue
    new = override.sub('\n', text)
    new = helper.sub('\n', new)
    new = re.sub(r'implements(\s+)CombatOpponentReferencingEffect(\s*)\{', r'implements\1CardEffect\2{', new)
    new = re.sub(r',\s*CombatOpponentReferencingEffect\b', '', new)
    new = re.sub(r'\bCombatOpponentReferencingEffect,\s*', '', new)
    new = re.sub(r'\n{3,}', '\n\n', new)
    if new != text:
        path.write_text(new)
        changed += 1

if iface.exists():
    iface.unlink()
    print('deleted', iface.name)
print('records changed:', changed)
left = [p.name for p in effect_dir.glob('*.java')
        if re.search(r'implements[^{]*CombatOpponentReferencingEffect|referencesCombatOpponent', p.read_text())]
if left:
    sys.exit('still referencing: ' + ', '.join(left))
