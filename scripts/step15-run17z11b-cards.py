#!/usr/bin/env python3
"""Step 15 run 17z11b: the cards whose effects read the damage-to-a-player event from the stack entry.

Idempotent, per letter module. Optional repository-root argument.

- Gishath, Sun's Avatar: "reveal that many cards" counts the entry's event value.
- Curse of Stalked Prey: "put a +1/+1 counter on that creature" is the triggering permanent's counter.
- Mask of the Schemer: "it connives X" is the triggering permanent conniving the event value.
- Ogre-Head Helm: "you may sacrifice it" sacrifices the creature that dealt the damage.
- Invocation of Saint Traft, Sticky Fingers: "enchanted creature has '…'" is a granted triggered ability.
"""
import os
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CARDS = os.path.join(ROOT, 'magical-vibes-card')


def path(letter, name):
    return os.path.join(CARDS, letter, 'src/main/java/com/github/laxika/magicalvibes/cards', letter, name + '.java')


def add_import(src, imp):
    line = 'import ' + imp + ';\n'
    if line in src:
        return src
    i = src.index('import ')
    return src[:i] + line + src[i:]


def drop_import(src, imp):
    line = 'import ' + imp + ';\n'
    simple = imp.rsplit('.', 1)[1]
    body = src.replace(line, '')
    return body if simple not in body else src


def edit(letter, name, old, new, add=(), drop=()):
    p = path(letter, name)
    with open(p) as f:
        src = f.read()
    if new in src:
        return
    if old not in src:
        sys.exit(f'{name}: expected text not found')
    src = src.replace(old, new)
    for imp in add:
        src = add_import(src, imp)
    for imp in drop:
        src = drop_import(src, imp)
    with open(p, 'w') as f:
        f.write(src)
    print('converted', name)


E = 'com.github.laxika.magicalvibes.model.effect.'

edit('g', 'GishathSunsAvatar',
     """                        null,
                        true
                ));""",
     """                        null,
                        true,
                        new EventValue()
                ));""",
     add=['com.github.laxika.magicalvibes.model.amount.EventValue'])

edit('c', 'CurseOfStalkedPrey',
     'addTrigger(Triggers.dealsCombatDamageToPlayer(), new PutCountersOnSourceEffect(1, 1, 1));',
     """addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING,
                        CounterType.PLUS_ONE_PLUS_ONE));""",
     add=[E + 'PutCounterOnReferencedPermanentEffect', E + 'PermanentReference',
          'com.github.laxika.magicalvibes.model.CounterType'],
     drop=[E + 'PutCountersOnSourceEffect'])

edit('m', 'MaskOfTheSchemer',
     'new DrawDiscardAndConniveEffect(new EventValue())',
     'DrawDiscardAndConniveEffect.ofTriggeringPermanent(new EventValue())')

edit('o', 'OgreHeadHelm',
     'new SacrificeSelfThenEffect(new DiscardOwnHandThenDrawEffect(new Fixed(3)))',
     'SacrificeSelfThenEffect.ofTriggeringPermanent(new DiscardOwnHandThenDrawEffect(new Fixed(3)))')

edit('i', 'InvocationOfSaintTraft',
     """                .addTrigger(Triggers.attacks(), new CreateTokenForTriggeringPlayerEffect(
                        new CreateTokenEffect(1, "Angel", 4, 4, CardColor.WHITE,
                                List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING), true, true)));""",
     """                .addStaticEffect(new GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.attacks(),
                        new CreateTokenEffect(1, "Angel", 4, 4, CardColor.WHITE,
                                List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING), true, true)),
                        GrantScope.ENCHANTED_CREATURE));""",
     add=[E + 'GrantTriggeredAbilityEffect', E + 'GrantScope', 'com.github.laxika.magicalvibes.model.TriggeredAbility'],
     drop=[E + 'CreateTokenForTriggeringPlayerEffect'])

edit('s', 'StickyFingers',
     """                .addTrigger(Triggers.dealsCombatDamageToPlayer(),
                        new CreateTokenForTriggeringPlayerEffect(CreateTokenEffect.ofTreasureToken(1)))""",
     """                .addStaticEffect(new GrantTriggeredAbilityEffect(new TriggeredAbility(
                        Triggers.dealsCombatDamageToPlayer(), CreateTokenEffect.ofTreasureToken(1)),
                        GrantScope.ENCHANTED_CREATURE))""",
     add=[E + 'GrantTriggeredAbilityEffect', 'com.github.laxika.magicalvibes.model.TriggeredAbility'],
     drop=[E + 'CreateTokenForTriggeringPlayerEffect'])
