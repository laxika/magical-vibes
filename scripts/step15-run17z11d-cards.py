#!/usr/bin/env python3
"""Step 15, run 17z11d: the "whenever you attack" and attack cards the attack collectors read by effect class.

Idempotent; run per letter module: `step15-run17z11d-cards.py <letter> [repo-root]` (or `all`).

- "They / those creatures gain [keyword]" of a "whenever you attack" ability (Valor's Reach, Tyvar the Bellicose, Love
  on the Battlefield, Frontier Warmonger, Angelic Guardian) is `GrantScope.DECLARED_ATTACKERS`, the entry's attacking
  creatures, not `GrantScope.TARGETS` rebuilt into declared recipients by the collector; Frontier Warmonger's "attack
  one of your opponents or a planeswalker they control" is the trigger's filter.
- Chatzuk, Mighty Guitarist's "whenever two or more creatures you control attack in a band" is
  `TriggerMultiplicity.EACH_BAND`.
- Yotian Courier writes its own modal: `ChooseOneAtTriggerTimeEffect(ChooseOneEffect.notChosenDuringLastCombat(...))`.
- Pip-Boy 3000's "whenever equipped creature attacks, choose one" is the Equipment's own printed ability, its "that
  creature" the triggering permanent.
"""
import pathlib
import re
import sys

letter = sys.argv[1] if len(sys.argv) > 1 else 'all'
root = pathlib.Path(sys.argv[2] if len(sys.argv) > 2 else '.')
base = root / 'magical-vibes-card'
letters = [p.name for p in sorted(base.iterdir()) if len(p.name) == 1 or p.name == 'shared'] if letter == 'all' else [letter]

M = 'com.github.laxika.magicalvibes.model.'
E = M + 'effect.'
T = M + 'trigger.'

DECLARED = ('GrantScope.TARGETS', 'GrantScope.DECLARED_ATTACKERS')

CARDS = {
    'ValorsReach': ('v', [DECLARED], [], []),
    'TyvarTheBellicose': ('t', [DECLARED], [], []),
    'LoveOnTheBattlefield': ('l', [
        ('new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGETS)',
         'new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.DECLARED_ATTACKERS)'),
        ("""        // The two attackers are fixed when the trigger fires: GrantScope.TARGETS carries the declared
        // attackers, so a creature that left combat before resolution still gains first strike.
""", """        // The two attackers are fixed when the trigger fires: GrantScope.DECLARED_ATTACKERS reads the entry's
        // attacking creatures, so a creature that left combat before resolution still gains first strike.
""")], [], []),
    'FrontierWarmonger': ('f', [(
        """addTrigger(Triggers.youAttack(), new GrantKeywordEffect(
                Keyword.MENACE, GrantScope.TARGETS, new PermanentIsAttackingOpponentOrTheirPlaneswalkerPredicate()));""",
        """addTrigger(Triggers.youAttack().withFilter(new PermanentIsAttackingOpponentOrTheirPlaneswalkerPredicate()),
                new GrantKeywordEffect(Keyword.MENACE, GrantScope.DECLARED_ATTACKERS));""")], [], []),
    'AngelicGuardian': ('a', [DECLARED], [], []),
    'ChatzukMightyGuitarist': ('c', [(
        'addTrigger(Triggers.youAttack(), new BoostCreaturesInAttackingBandsEffect());',
        'addTrigger(Triggers.youAttack(), TriggerOptions.DEFAULT.withMultiplicity(TriggerMultiplicity.EACH_BAND),\n'
        '                new BoostCreaturesInAttackingBandsEffect());')],
        [f'{T}TriggerOptions', f'{T}TriggerMultiplicity'], []),
    'YotianCourier': ('y', [
        ('addTrigger(Triggers.attacks(), new ChooseModeNotChosenDuringLastCombatEffect(List.of(',
         'addTrigger(Triggers.attacks(), new ChooseOneAtTriggerTimeEffect(ChooseOneEffect.notChosenDuringLastCombat(List.of('),
        ("""                                new ManaValueBound(powerstones, true, 0))))));
    }""",
         """                                new ManaValueBound(powerstones, true, 0)))))));
    }""")],
        [f'{E}ChooseOneAtTriggerTimeEffect'], [f'{E}ChooseModeNotChosenDuringLastCombatEffect']),
    'PipBoy3000': ('p', [
        ('new PutCountersOnSourceEffect(1, 1, 1)',
         'new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING,\n'
         '                                CounterType.PLUS_ONE_PLUS_ONE)'),
        ("""        addStaticEffect(new GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.attacks(),
                attackChoice), GrantScope.EQUIPPED_CREATURE));""",
         """        addTrigger(Triggers.attacks(), attackChoice);""")],
        [f'{E}PutCounterOnReferencedPermanentEffect', f'{E}PermanentReference', M + 'CounterType'],
        [f'{E}PutCountersOnSourceEffect', f'{E}GrantTriggeredAbilityEffect', f'{E}GrantScope',
         M + 'TriggeredAbility']),
}


def ensure_import(text, fqcn):
    if f'import {fqcn};' in text:
        return text
    imports = list(re.finditer(r'^import ([\w.]+);\n', text, re.M))
    non_java = [m for m in imports if not m.group(1).startswith('java.')]
    after = [m for m in non_java if m.group(1) > fqcn]
    if after:
        pos = after[0].start()
    elif non_java:
        pos = non_java[-1].end()
    else:
        pos = imports[0].start()
    return text[:pos] + f'import {fqcn};\n' + text[pos:]


def drop_unused_import(text, fqcn):
    simple = fqcn.rsplit('.', 1)[1]
    line = f'import {fqcn};\n'
    if line in text and len(re.findall(rf'\b{simple}\b', text.replace(line, ''))) == 0:
        text = text.replace(line, '')
    return text


changed = 0
for name, (directory, edits, adds, drops) in CARDS.items():
    if directory not in letters:
        continue
    path = base / directory / 'src/main/java/com/github/laxika/magicalvibes/cards' / directory / f'{name}.java'
    text = path.read_text()
    original = text
    for old, new in edits:
        if new in text:
            continue
        if old not in text:
            sys.exit(f'{name}: expected text not found: {old!r}')
        text = text.replace(old, new)
    for fqcn in adds:
        text = ensure_import(text, fqcn)
    for fqcn in drops:
        text = drop_unused_import(text, fqcn)
    if text != original:
        path.write_text(text)
        changed += 1
        print(f'converted {name}')
print(f'{changed} card(s) changed')
