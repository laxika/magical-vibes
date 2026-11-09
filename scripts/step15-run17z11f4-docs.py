#!/usr/bin/env python3
"""Step 15, run 17z11f4: agent-docs rows of the forms the run changed or removed.

Idempotent; fails if a doc still names a removed form outside a "Gone" line. Optional repository-root argument.
Each edit replaces the one line holding its key (a substring unique to that line) with the new text.
"""
import pathlib
import sys

root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else '.')
DOCS = root / 'agent-docs'

CLASH = ('the clash\'s outcome is the ability\'s event value whatever its effects are, 1 if you won (CR 701.30d), '
         'else 0: "whenever you clash and win" / "whenever you win a clash" is '
         '`TriggerOptions.DEFAULT.withEventCondition(new EventValueAtLeast(1))`, "if you won" '
         '`new ConditionalEffect(new EventValueAtLeast(1), …)` read from the entry as the ability resolves')

EDITS = {
    'CARD_PATTERNS_CREATURES_TRIGGERED.md': [
        ('`CombatDamageService` binds it into the effect\'s `chosenCardId`',
         '| Combat damage → random own-graveyard instant/sorcery, free cast at next upkeep | `p/PowerPack.java` | Triggers.dealsCombatDamageToPlayer() `ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect()` — the target card is chosen at random as the trigger goes on the stack and is the entry\'s `targetId` (graveyard zone); the ability does nothing if that card has left the graveyard, then exiled and offered through the shared rebound-style next-upkeep free-cast trigger; cast cards are exiled instead of going to a graveyard |'),
    ],
    'EFFECT_MERGE_PLAN.md': [
        ('- `IfWonClashEffect` / `IfLostClashEffect` — the pair deliberately models both branches.',
         '- Gone (step 15 of effect-slot-refactor.md, run 17z11f4): `IfWonClashEffect` / `IfLostClashEffect`; "if you won" is a `ConditionalEffect` on the clash trigger\'s event value.'),
    ],
    'EFFECTS_INDEX.md': [
        ('| `ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect` |',
         '| `ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect` | `()` | exiles the random instant or sorcery chosen as the combat-damage trigger goes on the stack (the entry\'s `targetId`, in the graveyard) from the controller\'s graveyard and queues the exiled card for a free-cast offer at that controller\'s next upkeep; a cast card is exiled instead of going to a graveyard |'),
        ('| `ClashOutcomeConditionalEffect` |',
         '| Gone: `ClashOutcomeConditionalEffect` (step 15, run 17z11f4) | | "Whenever you clash": ' + CLASH + '. |'),
        ('| `GainLifeEqualToControlledCreatureCombatDamageEffect` |',
         '| Gone: `GainLifeEqualToControlledCreatureCombatDamageEffect` (step 15, run 17z11f4) | | Noble Purpose\'s "whenever a creature you control deals combat damage, you gain that much life" is `Triggers.creatureYouControlDealsCombatDamage()` + `GainLifeEffect(new EventValue())` |'),
        ('| `TransformSelfWhenChosenPermanentLeavesEffect` |',
         '| Gone: `TransformSelfWhenChosenPermanentLeavesEffect` (step 15, run 17z11f4) | | "When the chosen creature leaves the battlefield, transform this" is `Triggers.leavesBattlefield(new PermanentIsChosenPermanentPredicate(), ObjectRelation.ANOTHER)` + `TransformSelfEffect()` (Zenos yae Galvus, Havengul Mystery) |'),
        ('| `IfWonClashEffect` |',
         '| Gone: `IfWonClashEffect`, `IfLostClashEffect` (step 15, run 17z11f4) | | ' + CLASH + ' |'),
        ('| `IfLostClashEffect` |', None),
        ('| `SearchBlueOrBlackCreatureToBattlefieldOnAllyCombatDamageEffect` |',
         '| Gone: `SearchBlueOrBlackCreatureToBattlefieldOnAllyCombatDamageEffect` (step 15, run 17z11f4) | | Kaito Shizuki\'s emblem is `CreateEmblemEffect(new TriggeredAbility(Triggers.creatureYouControlDealsCombatDamageToPlayer(), new SearchLibraryEffect(blueOrBlackCreature, BATTLEFIELD)), …)` |'),
    ],
    'EFFECTS_QUICK_REFERENCE.md': [
        ('- `TransformSelfWhenChosenPermanentLeavesEffect()`',
         '- Gone: `TransformSelfWhenChosenPermanentLeavesEffect` (step 15, run 17z11f4): "when the chosen creature leaves the battlefield, transform this" is `Triggers.leavesBattlefield(new PermanentIsChosenPermanentPredicate(), ObjectRelation.ANOTHER)` + `TransformSelfEffect()`. Zenos yae Galvus; Havengul Mystery'),
        ('- `ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect()`',
         '- `ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect()` — exile a random instant or sorcery from the controller\'s graveyard (chosen as the trigger goes on the stack, the entry\'s `targetId`; no effect if it left the graveyard), then offer it for a free cast at that controller\'s next upkeep; if cast, it is exiled instead of going to a graveyard'),
        ('- `ClashOutcomeConditionalEffect` — clash-trigger effect applied only on one outcome; `wrapped()` +',
         '- Gone: `ClashOutcomeConditionalEffect`, `IfWonClashEffect`, `IfLostClashEffect` (step 15, run 17z11f4): ' + CLASH),
        ('  `appliesOnWin()`. Read by `TriggerCollectionService.fireClashTriggers` to resolve the won/lost', None),
        ('  clause without branching on the two wrapper types. Impl `IfWonClashEffect`, `IfLostClashEffect`', None),
        ('`boundedByEventValue()` for "less than or equal to that damage / that creature\'s power"',
         '- `PutCardToBattlefieldEffect(...).boundedBy(DynamicAmount)` / `.boundedByEventValue()` — only a card with mana value at most the amount, evaluated as the effect resolves, is eligible: `boundedBy(new TriggeringPermanentManaValue())` for "with equal or lesser mana value" (Kodama of the East Tree), `boundedBy(new TriggeringPermanentPower(true))` for "less than or equal to that creature\'s power", which can be negative (Minn, Wily Illusionist), `boundedByEventValue()` for "less than or equal to that damage" (Broodcaller Scourge, Covert Technician)'),
        ('- `SearchBlueOrBlackCreatureToBattlefieldOnAllyCombatDamageEffect`',
         '- Gone: `SearchBlueOrBlackCreatureToBattlefieldOnAllyCombatDamageEffect` (step 15, run 17z11f4): Kaito Shizuki\'s emblem is a `TriggeredAbility` on `Triggers.creatureYouControlDealsCombatDamageToPlayer()` with a `SearchLibraryEffect`'),
        ('- `IfWonClashEffect(wrapped)`', None),
        ('- `IfLostClashEffect(wrapped)`', None),
    ],
    'ORACLE_TEXT_EFFECT_MAP.md': [
        ('`ChooseOpponentCreatureThenBoostOthersEffect(-2, -2)` + `TransformSelfWhenChosenPermanentLeavesEffect()`',
         '| "When Zenos yae Galvus enters, choose a creature an opponent controls. Until end of turn, creatures other than Zenos yae Galvus and the chosen creature get -2/-2. When the chosen creature leaves the battlefield, transform Zenos yae Galvus." | `ChooseOpponentCreatureThenBoostOthersEffect(-2, -2)` + `TransformSelfEffect()` | Triggers.enters(ObjectRelation.THIS) + Triggers.leavesBattlefield(new PermanentIsChosenPermanentPredicate(), ObjectRelation.ANOTHER) | Zenos yae Galvus (FIN) |'),
        ('`CreateEmblemEffect(List.of(new SearchBlueOrBlackCreatureToBattlefieldOnAllyCombatDamageEffect.Marker()), "…")`',
         '| "Whenever a creature you control deals combat damage to a player, search your library for a blue or black creature card, put it onto the battlefield, then shuffle." | `CreateEmblemEffect(new TriggeredAbility(Triggers.creatureYouControlDealsCombatDamageToPlayer(), new SearchLibraryEffect(blueOrBlackCreature, LibrarySearchDestination.BATTLEFIELD)), "…")` | emblem trigger | Kaito Shizuki −7. `TriggerCollectionService.checkEmblemAllyCreatureCombatDamageToPlayerTriggers` puts the emblem\'s ability on the stack for each of its controller\'s creatures dealing combat damage to a player, recording the creature, the player and the damage |'),
        ('(bound into `chosenCardId` by `CombatDamageService`)',
         '| "Whenever this creature deals combat damage to a player, exile target instant or sorcery card from your graveyard chosen at random. At the beginning of your next upkeep, you may cast that card without paying its mana cost. If that spell would be put into your graveyard, exile it instead." | `ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect()` | `Triggers.dealsCombatDamageToPlayer()` | Power Pack (MSC 90). The target card is selected randomly when the combat-damage trigger is put on the stack (the entry\'s `targetId`) and nothing happens if it left the graveyard; the delayed offer uses the shared next-upkeep free-cast pipeline and marks the cast card for exile instead of a graveyard |'),
        ('| "Whenever you clash, [effect]. If you won, [win-only effect]." |',
         '| "Whenever you clash, [effect]. If you won, [win-only effect]." | `addTrigger(Triggers.youClash(), effect, new ConditionalEffect(new EventValueAtLeast(1), winOnlyEffect))` | `Triggers.youClash()` | Clash (LRW, CR 701.30). Fired by `TriggerCollectionService.fireClashTriggers` after the clash ends; ' + CLASH + '. Targeting triggers route through `ClashTriggerTarget` (target creature an opponent controls). Inert until some card *initiates* a clash (see `ClashEffect`). E.g. Entangling Trap: `TapPermanentsEffect(TARGET)` + `ConditionalEffect(new EventValueAtLeast(1), SkipNextUntapEffect(TARGET))`; "whenever you clash and win" (Sylvan Echoes, Marvo, Deep Operative) is the event condition |'),
        ('| "Whenever you clash, you may pay {C}. If you do, [effect]. If you won, [outcome-dependent detail]." |',
         '| "Whenever you clash, you may pay {C}. If you do, [effect]. If you won, [outcome-dependent detail]." | `addTrigger(Triggers.youClash(), new ConditionalEffect(new EventValueAtLeast(1), new MayPayManaEffect("{C}", wonVariant, prompt)), new ConditionalEffect(new NotCondition(new EventValueAtLeast(1)), new MayPayManaEffect("{C}", lostVariant, prompt)))` | `Triggers.youClash()` | Clash trigger whose base effect happens either way but a detail differs by outcome: exactly one "if" holds as it resolves. E.g. Rebellion of the Flamekin: create a 3/1 red Elemental Shaman token that gains `HASTE` (`grantedKeywordsUntilEndOfTurn`) only on the won branch |'),
        ('| "Whenever a creature you control deals combat damage, you gain that much life" | `GainLifeEqualToControlledCreatureCombatDamageEffect()`',
         '| "Whenever a creature you control deals combat damage, you gain that much life" | `GainLifeEffect(new EventValue())` | `Triggers.creatureYouControlDealsCombatDamage()` | Noble Purpose. Fires for combat damage to any target, once per Noble Purpose; "that much" is the damage the creature dealt, the entry\'s event value |'),
        ('| "you get an emblem with \\"whenever a creature dies, return it to the battlefield under your control at the beginning of the next end step\\"" |',
         '| "you get an emblem with \\"whenever a creature dies, return it to the battlefield under your control at the beginning of the next end step\\"" | `CreateEmblemEffect(new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANY), new RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null)), "...")` | loyalty ability | Liliana, Defiant Necromancer. Abilities of emblems function in the command zone (CR 114.4); `TriggerCollectionService#collectEmblemCreatureDeathTriggers` reads the emblems\' death abilities on every creature death and records the creature that died (its card, last-known permanent and controller) on the entry |'),
    ],
}

REMOVED = ['ClashOutcomeConditionalEffect', 'IfWonClashEffect', 'IfLostClashEffect',
           'TransformSelfWhenChosenPermanentLeavesEffect', 'GainLifeEqualToControlledCreatureCombatDamageEffect',
           'SearchBlueOrBlackCreatureToBattlefieldOnAllyCombatDamageEffect', 'chosenCardId']


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        lines = path.read_text().split('\n')
        for key, new in edits:
            hits = [i for i, line in enumerate(lines) if key in line]
            if new is not None and new in lines:
                continue
            if not hits:
                if new is None:
                    continue
                raise SystemExit(f'{name}: no line holds {key!r}')
            if len(hits) > 1:
                raise SystemExit(f'{name}: {len(hits)} lines hold {key!r}')
            if new is None:
                del lines[hits[0]]
            else:
                lines[hits[0]] = new
        path.write_text('\n'.join(lines))
    bad = []
    for path in DOCS.glob('*.md'):
        for n, line in enumerate(path.read_text().split('\n'), 1):
            if any(form in line for form in REMOVED) and 'Gone' not in line:
                bad.append(f'{path.name}:{n}')
    if bad:
        raise SystemExit('still named: ' + ', '.join(bad))


if __name__ == '__main__':
    main()
