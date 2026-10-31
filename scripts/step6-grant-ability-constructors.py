#!/usr/bin/env python3
"""EffectSlot refactor step 6: grant records gain an Ability-taking twin of each slot constructor.

Usage: step6-grant-ability-constructors.py

For every record in the effect package whose first component is ``Ability grantedAbility``, each
constructor ``R(EffectSlot slot, CardEffect grantedEffect, rest...)`` whose body is
``this(LegacySlots.grant(slot, grantedEffect), ...)`` or ``this(slot, grantedEffect, ...)`` gets a
twin ``R(Ability grantedAbility, rest...)`` with the same body on the ability, unless one exists, so a
static ability, which has no slot, can be granted through the same shapes. Prints each changed file.
"""
import pathlib
import re

EFFECTS = pathlib.Path('magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect')
CTOR = re.compile(r'(?P<doc>(?:[ \t]*/\*\*(?:(?!\*/).)*\*/\n)?)[ \t]*public (?P<name>\w+)\(\s*EffectSlot slot,\s*'
                  r'CardEffect grantedEffect(?P<rest>(?:,[^)]*)?)\)\s*\{\s*this\((?:LegacySlots\.grant\(slot, '
                  r'grantedEffect\)|slot, grantedEffect)(?P<args>[^;]*)\);\s*\}\n', re.S)


def param_types(rest):
    return [p.strip().rsplit(' ', 1)[0] for p in re.split(r',(?![^<]*>)', rest) if p.strip()]


for path in sorted(EFFECTS.glob('*.java')):
    src = path.read_text(encoding='utf-8')
    if not re.search(r'public record \w+\(\s*Ability grantedAbility\b', src):
        continue
    existing = set()
    for m in re.finditer(r'public ' + path.stem + r'\(\s*Ability grantedAbility([^)]*)\)', src):
        existing.add(tuple(param_types(m.group(1))))
    canonical = re.search(r'public record ' + path.stem + r'\(\s*Ability grantedAbility([^)]*)\)', src)
    existing.add(tuple(param_types(canonical.group(1))))
    additions = []
    for m in CTOR.finditer(src):
        types = tuple(param_types(m.group('rest')))
        if m.group('name') != path.stem or types in existing:
            continue
        existing.add(types)
        rest = m.group('rest')
        additions.append((m.end(), f"""
    /** The grant of {{@code grantedAbility}}, such as a static ability, which has no slot. */
    public {path.stem}(Ability grantedAbility{rest}) {{
        this(grantedAbility{m.group('args')});
    }}
"""))
    for end, text in reversed(additions):
        src = src[:end] + text + src[end:]
    if additions:
        path.write_text(src, encoding='utf-8')
        print(path)
