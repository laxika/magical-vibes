#!/usr/bin/env python3
"""Step 15, run 17z11c2: the "a creature (you control) becomes blocked" watchers read the blocked creature.

Idempotent; run per letter module: `step15-run17z11c2-cards.py <letter> [repo-root]` (or `all`).

- The "whenever a [filter] creature (you control) becomes blocked" cards' "it" / "that creature" read the entry's
  triggering permanent instead of a self effect whose source the collector rebound to the blocked creature:
  `BoostSelfEffect` -> `BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, ...)`, its
  `CreaturesBlockingSource()` -> `CreaturesBlockingSource(true)`; `PutCountersOnSelfEffect` ->
  `PutCounterOnReferencedPermanentEffect(TRIGGERING, ...)`; `GrantKeywordEffect(..., SELF)` -> `TRIGGERING_PERMANENT`;
  `ReturnToHandEffect.self()` -> `.triggering()`; Gustcloak Savior's untap and removal from combat; Ib Halfheart's
  sacrifice and damage (`SacrificeSelfThenEffect.ofTriggeringPermanent`, `.dealtByTriggeringPermanent()`); Labyrinth
  Raptor's `PermanentBlockingSourcePredicate.ofTriggeringPermanent()`.
- Retaliation grants its ability ("creatures you control have '...'", `GrantTriggeredAbilityEffect` with
  `GrantScope.ALL_OWN_CREATURES`).
- No Quarter and Righteous Indignation: "becomes blocked by a creature" (`EACH_BLOCKER`) with the pair's power
  comparison the event condition `BlockPairLesserPower` and the attacker's colors the trigger's filter; "the blocking
  creature" is the entry's "that creature" (`DestroyCombatOpponentEffect`, `BoostTargetCreatureEffect` with no
  declared target), "the attacking creature" the triggering permanent.
- Mirror Shield's and Zombie Boa's granted "becomes blocked by a [filter] creature" abilities say `EACH_BLOCKER`.
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
F = M + 'filter.'
T = M + 'trigger.'
C = M + 'condition.'
A = M + 'amount.'

BOOST_REF = f'{E}BoostReferencedPermanentEffect'
REF = f'{E}PermanentReference'

# card class -> (letter dir, [(old, new)], [imports to add], [imports to drop when unused])
CARDS = {
    'CunningEvasion': ('c', [('ReturnToHandEffect.self()', 'ReturnToHandEffect.triggering()')], [], []),
    'GrazilaxxIllithidScholar': ('g', [('ReturnToHandEffect.self()', 'ReturnToHandEffect.triggering()')], [], []),
    'GeneralMarhaultElsdragon': ('g', [(
        """new BoostSelfEffect(
                new Scaled(new CreaturesBlockingSource(), 3),
                new Scaled(new CreaturesBlockingSource(), 3)));""",
        """new BoostReferencedPermanentEffect(
                PermanentReference.TRIGGERING,
                new Scaled(new CreaturesBlockingSource(true), 3),
                new Scaled(new CreaturesBlockingSource(true), 3)));""")],
        [BOOST_REF, REF], [f'{E}BoostSelfEffect']),
    'GustcloakSavior': ('g', [
        ('new UntapPermanentsEffect(TapUntapScope.SELF)', 'new UntapPermanentsEffect(TapUntapScope.TRIGGERING)'),
        ('new RemoveSelfFromCombatEffect()', 'new RemoveReferencedPermanentFromCombatEffect(PermanentReference.TRIGGERING)')],
        [f'{E}RemoveReferencedPermanentFromCombatEffect', REF], [f'{E}RemoveSelfFromCombatEffect']),
    'IbHalfheartGoblinTactician': ('i', [(
        """new SacrificeSelfThenEffect(new DealDamageToEachMatchingPermanentEffect(
                        4, new PermanentBlockingSourcePredicate(), EachPermanentScope.ALL_PLAYERS)));""",
        """SacrificeSelfThenEffect.ofTriggeringPermanent(new DealDamageToEachMatchingPermanentEffect(
                        4, new PermanentBlockingSourcePredicate(), EachPermanentScope.ALL_PLAYERS)
                        .dealtByTriggeringPermanent()));""")], [], []),
    'LabyrinthRaptor': ('l', [('new PermanentBlockingSourcePredicate()',
                               'PermanentBlockingSourcePredicate.ofTriggeringPermanent()')], [], []),
    'Retaliation': ('r', [(
        'addTrigger(Triggers.creatureYouControlBecomesBlocked(), TriggerOptions.EACH_BLOCKER, new BoostSelfEffect(1, 1));',
        """addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, new BoostSelfEffect(1, 1)),
                GrantScope.ALL_OWN_CREATURES));""")],
        [f'{E}GrantTriggeredAbilityEffect', f'{E}GrantScope', M + 'TriggeredAbility'], []),
    'SeiferBalambRival': ('s', [('new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)',
                                 'new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TRIGGERING_PERMANENT)')], [], []),
    'SheHulkWallbreaker': ('s', [(
        'new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new CreaturesBlockingSource())',
        'new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE,\n'
        '                        new CreaturesBlockingSource(true))')],
        [f'{E}PutCounterOnReferencedPermanentEffect', REF], [f'{E}PutCountersOnSelfEffect']),
    'SomberwaldAlpha': ('s', [
        ("""        // Whenever a creature you control becomes blocked, it gets +1/+1 until end of turn.
        // "it" is the blocked creature, set as the non-targeting source of the trigger.
        addTrigger(Triggers.creatureYouControlBecomesBlocked(), new BoostSelfEffect(1, 1));""",
         """        // Whenever a creature you control becomes blocked, it gets +1/+1 until end of turn.
        addTrigger(Triggers.creatureYouControlBecomesBlocked(),
                new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, 1, 1));""")],
        [BOOST_REF, REF], [f'{E}BoostSelfEffect']),
    'TatteredRatter': ('t', [('new BoostSelfEffect(2, 0)',
                              'new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, 2, 0)')],
                       [BOOST_REF, REF], [f'{E}BoostSelfEffect']),
    'UnstoppableAsh': ('u', [('new BoostSelfEffect(0, 5)',
                              'new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, 0, 5)')],
                       [BOOST_REF, REF], [f'{E}BoostSelfEffect']),
    'SpinedSliver': ('s', [('new BoostSelfEffect(new CreaturesBlockingSource(), new CreaturesBlockingSource())',
                            'new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING,\n'
                            '                        new CreaturesBlockingSource(true), new CreaturesBlockingSource(true))')],
                     [BOOST_REF, REF], [f'{E}BoostSelfEffect']),
    'BerserkMurlodont': ('b', [('new BoostSelfEffect(new CreaturesBlockingSource(), new CreaturesBlockingSource())',
                                'new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING,\n'
                                '                        new CreaturesBlockingSource(true), new CreaturesBlockingSource(true))')],
                         [BOOST_REF, REF], [f'{E}BoostSelfEffect']),
    'NoQuarter': ('n', [(
        """        addTrigger(Triggers.creatureBecomesBlocked(),
                new DestroyWeakerBlockParticipantEffect(BlockParticipant.BLOCKER));""",
        """        addTrigger(Triggers.creatureBecomesBlocked(),
                TriggerOptions.EACH_BLOCKER.withEventCondition(new BlockPairLesserPower(BlockParticipant.BLOCKER)),
                new DestroyCombatOpponentEffect(false));"""), (
        """        addTrigger(Triggers.creatureBecomesBlocked(),
                new DestroyWeakerBlockParticipantEffect(BlockParticipant.ATTACKER));""",
        """        addTrigger(Triggers.creatureBecomesBlocked(),
                TriggerOptions.EACH_BLOCKER.withEventCondition(new BlockPairLesserPower(BlockParticipant.ATTACKER)),
                new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));""")],
        [f'{E}DestroyCombatOpponentEffect', f'{E}DestroyReferencedPermanentEffect', REF, f'{T}TriggerOptions',
         f'{C}BlockPairLesserPower'], [f'{E}DestroyWeakerBlockParticipantEffect']),
    'RighteousIndignation': ('r', [(
        """        addTrigger(Triggers.creatureBecomesBlocked(),
                new BoostBlockerWhenAttackerMatchesEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.BLACK, CardColor.RED)), 1, 1));""",
        """        // "Whenever a creature blocks a black or red creature, the blocking creature gets +1/+1": for each blocker
        // of a black or red attacker, the blocker the entry's "that creature".
        addTrigger(Triggers.creatureBecomesBlocked()
                        .withFilter(new PermanentColorInPredicate(Set.of(CardColor.BLACK, CardColor.RED))),
                TriggerOptions.EACH_BLOCKER,
                new BoostTargetCreatureEffect(1, 1));""")],
        [f'{E}BoostTargetCreatureEffect', f'{T}TriggerOptions'], [f'{E}BoostBlockerWhenAttackerMatchesEffect']),
    'MirrorShield': ('m', [(
        'new TriggeredAbility(Triggers.becomesBlocked().withFilter(deathtouch), destroyDeathtouchCreature)',
        'new TriggeredAbility(Triggers.becomesBlocked().withFilter(deathtouch), TriggerOptions.EACH_BLOCKER,\n'
        '                        destroyDeathtouchCreature)')], [f'{T}TriggerOptions'], []),
    'ZombieBoa': ('z', [(
        """new TriggeredAbility(Triggers.becomesBlocked(),
                                new DestroyCreatureBlockingThisEffect()))""",
        """new TriggeredAbility(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER,
                                new DestroyCreatureBlockingThisEffect()))""")], [f'{T}TriggerOptions'], []),
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
