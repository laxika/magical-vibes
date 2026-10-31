"""EffectSlot refactor step 4: rewrite the slot-map merges into GameData.perpetualTriggeredAbilityGrants
as calls to GameData.grantPerpetualAbility / grantPerpetualAbilityOnce. Prints the files it could not
rewrite; those are converted by hand."""
import re
import subprocess
import sys

START = re.compile(r'gameData\.perpetualTriggeredAbilityGrants\.compute\((?P<id>[^,]+), \(ignored, existing\) -> \{')


def block_end(s, i):
    depth = 0
    j = i
    while True:
        c = s[j]
        if c == '{':
            depth += 1
        elif c == '}':
            depth -= 1
            if depth == 0:
                assert s[j:j + 3] == '});', s[j:j + 20]
                return j + 3
        j += 1


def rewrite(body, card_id):
    for declared in re.findall(r'^\s*(?:final\s+)?[A-Z][\w.<>, ]*\s+(\w+)\s*=(?!=)\s*(.*)$', body, re.M):
        name, value = declared
        if name != 'updated' and 'computeIfAbsent' not in value:
            return None
    slot = re.search(r'computeIfAbsent\(\s*([^,]+?),\s*ignored\w*\s*->', body, re.S)
    if not slot:
        return None
    slot = ' '.join(slot.group(1).split())
    once = '.contains(' in body
    single = re.findall(r'\.add\(([^;]+?)\);', body)
    many = re.findall(r'\.addAll\(([^;]+?)\);', body)
    if len(single) + len(many) != 1:
        return None
    if single:
        effect = ' '.join(single[0].split())
        method = 'grantPerpetualAbilityOnce' if once else 'grantPerpetualAbility'
        return f'gameData.{method}({card_id}, {slot}, {effect});'
    if once:
        return None
    effects = ' '.join(many[0].split())
    return f'{effects}.forEach(granted -> gameData.grantPerpetualAbility({card_id}, {slot}, granted));'


def main():
    files = subprocess.run(['/usr/bin/grep', '-rl', 'perpetualTriggeredAbilityGrants.compute(', '--include=*.java',
                            'magical-vibes-engine/src/main'], capture_output=True, text=True).stdout.split()
    manual = []
    for path in files:
        s = open(path).read()
        out, pos, ok = [], 0, True
        for m in START.finditer(s):
            end = block_end(s, m.end() - 1)
            new = rewrite(s[m.end():end], m.group('id').strip())
            if new is None:
                ok = False
                continue
            out.append(s[pos:m.start()] + new)
            pos = end
        out.append(s[pos:])
        open(path, 'w').write(''.join(out))
        if not ok:
            manual.append(path)
    print('\n'.join(manual))


if __name__ == '__main__':
    sys.exit(main())
