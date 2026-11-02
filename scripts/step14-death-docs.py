#!/usr/bin/env python3
"""Step 14, run 2 of effect-slot-refactor.md: rewrites the agent-docs rows that describe step 11's creature-death
abilities with trigger-only wrappers, the way scripts/step14-death-options.py rewrote the cards: the dying-creature
condition becomes the trigger's filter, "triggers only once each turn" a TriggerLimit, and "this creature or another
[restricted] creature" one THIS_OR_ANOTHER[_YOU_CONTROL] ability. Rows of the cards that keep their wrapper
(Oathkeeper, Kami of Mourning, Agyrem, The Master) and of other trigger families are left alone. Each replacement must
match exactly once; one whose new text is already present is skipped, so the script can be run again.
Usage: python3 scripts/step14-death-docs.py
"""
import sys

DOCS = "agent-docs/"
BAD = "�"

REPLACEMENTS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("`TriggeringPermanentConditionalEffect(PermanentAnyOfPredicate(List.of(PermanentIsEnchantedPredicate, "
         "PermanentIsEquippedPredicate)), ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect())` on "
         "`Triggers.creatureDies(THIS_OR_ANOTHER_YOU_CONTROL)` (the \"if it was enchanted or equipped\" check applies to "
         "Cass too)",
         "`ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect()` on one `TriggeredAbility` with two trigger "
         "conditions, `Triggers.dies(enchantedOrEquipped, THIS)` and `Triggers.creatureDies(enchantedOrEquipped, "
         "ANOTHER_YOU_CONTROL)` with `enchantedOrEquipped = PermanentAnyOfPredicate(List.of(PermanentIsEnchantedPredicate, "
         "PermanentIsEquippedPredicate))` (the \"if it was enchanted or equipped\" check applies to Cass too, which a "
         "`THIS_OR_ANOTHER_YOU_CONTROL` filter wouldn't do)"),
        ("| Triggers.creatureDies(THIS_OR_ANOTHER_YOU_CONTROL) | Cass, Hand of Vengeance (PIP 97) |",
         "| Triggers.dies(THIS) + Triggers.creatureDies(ANOTHER_YOU_CONTROL) | Cass, Hand of Vengeance (PIP 97) |"),
        ("+ targeted `Triggers.dies(THIS)` / `Triggers.creatureDies(ANOTHER_YOU_CONTROL)` with "
         "`TriggeringCardConditionalEffect(CardSubtypePredicate(LIZARD), …)` `SequenceEffect.of(LoseLifeEffect(1, "
         "TARGET_PLAYER), GainLifeEffect(1))`",
         "+ targeted `Triggers.creatureDies(PermanentHasSubtypePredicate(LIZARD), THIS_OR_ANOTHER_YOU_CONTROL)` "
         "`SequenceEffect.of(LoseLifeEffect(1, TARGET_PLAYER), GainLifeEffect(1))`"),
        ("-> `TriggeringPermanentConditionalEffect(PermanentIsSuspectedPredicate, ConditionalEffect(SourceCardInGraveyard, "
         "LoseLifeEffect(1, DYING_CREATURE_CONTROLLER)))` on `TriggerCondition(Triggers.creatureDies(OPPONENT_CONTROLS), "
         "GRAVEYARD)` (Snarlfang Vermin).",
         "-> `LoseLifeEffect(1, DYING_CREATURE_CONTROLLER)` on `TriggerCondition(Triggers.creatureDies("
         "PermanentIsSuspectedPredicate, OPPONENT_CONTROLS), GRAVEYARD)`, kept in "
         "`ConditionalEffect(SourceCardInGraveyard, …)` (Snarlfang Vermin)."),
        ("-> `Triggers.creatureDies(ANOTHER_YOU_CONTROL)` + `TriggeringCardConditionalEffect(CardIsMulticoloredPredicate(), "
         "RegisterDelayedReturnCardFromGraveyardToHandEffect(null))` (Rienne, Angel of Rebirth).",
         "-> `Triggers.creatureDies(PermanentIsMulticoloredPredicate(), ANOTHER_YOU_CONTROL)` + "
         "`RegisterDelayedReturnCardFromGraveyardToHandEffect(null)` (Rienne, Angel of Rebirth)."),
        ("-> `Triggers.creatureDies(ANOTHER_YOU_CONTROL)` + `TriggeringPermanentConditionalEffect("
         "PermanentHasCountersPredicate(IMPOSTOR), SequenceEffect.of(ExileTriggeringCreatureFromGraveyardEffect(), "
         "ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardAllOfPredicate(CREATURE, NOT SELF), 1, false, false)))` "
         "(Illicit Masquerade).",
         "-> `Triggers.creatureDies(PermanentHasCountersPredicate(IMPOSTOR), YOU_CONTROL)` + "
         "`SequenceEffect.of(ExileTriggeringCreatureFromGraveyardEffect(), "
         "ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardAllOfPredicate(CREATURE, NOT SELF), 1, false, false))` "
         "(Illicit Masquerade)."),
        ("| `LookAtTopCardMayRevealMatchingToHandEffect(CardSubtypePredicate(subtype), true)` on both "
         "`Triggers.creatureDies(ANOTHER_YOU_CONTROL)` (wrapped in `TriggeringCardConditionalEffect("
         "CardSubtypePredicate(subtype), …)`) and `Triggers.dies(THIS)` | trigger | Archghoul of Thraben (Zombie). "
         "\"This creature\" triggers whatever its types are, so it stays its own `Triggers.dies(THIS)` ability while "
         "the [Subtype] restriction of \"another\" is still a wrapper.",
         "| `LookAtTopCardMayRevealMatchingToHandEffect(CardSubtypePredicate(subtype), true)` on "
         "`Triggers.creatureDies(PermanentHasSubtypePredicate(subtype), THIS_OR_ANOTHER_YOU_CONTROL)` | trigger | "
         "Archghoul of Thraben (Zombie). One ability: \"this creature\" triggers whatever its types are, since "
         "`THIS_OR_ANOTHER_YOU_CONTROL` matches the source whatever it was, while the [Subtype] filter restricts "
         "\"another\"."),
        ("| `Triggers.creatureDies(YOU_CONTROL)` + `TriggeringCardConditionalEffect(CardNotPredicate(CardSubtypePredicate("
         "subtype)), RegisterTransformSourceAtNextUpkeepEffect())` | trigger | Archangel Avacyn",
         "| `Triggers.creatureDies(PermanentNotPredicate(PermanentHasSubtypePredicate(subtype)), YOU_CONTROL)` + "
         "`RegisterTransformSourceAtNextUpkeepEffect()` | trigger | Archangel Avacyn"),
        ("| `Triggers.creatureDies(OPPONENT_CONTROLS)` + `TriggeringCardConditionalEffect(CardNotPredicate("
         "CardIsTokenPredicate), ExileTriggeringCreatureAndTrackWithSourceEffect())`",
         "| `Triggers.creatureDies(PermanentNotPredicate(PermanentIsTokenPredicate), OPPONENT_CONTROLS)` + "
         "`ExileTriggeringCreatureAndTrackWithSourceEffect()`"),
        ("| `Triggers.creatureDies(ANOTHER)` with `TriggeringCardConditionalEffect(CardSubtypePredicate(GOBLIN), "
         "MayPayManaEffect(\"{2}{R}\", RevealTopCardCreatureToBattlefieldElseGraveyardEffect(CardAllOfPredicate("
         "CardIsPermanentPredicate, CardSubtypePredicate(GOBLIN))), prompt))` plus the same `MayPayManaEffect` on "
         "`Triggers.dies(THIS)` | trigger | Skirk Drill Sergeant. The global watcher covers another Goblin, while "
         "`Triggers.dies(THIS)` covers the source itself;",
         "| `Triggers.creatureDies(PermanentHasSubtypePredicate(GOBLIN), THIS_OR_ANOTHER)` with "
         "`MayPayManaEffect(\"{2}{R}\", RevealTopCardCreatureToBattlefieldElseGraveyardEffect(CardAllOfPredicate("
         "CardIsPermanentPredicate, CardSubtypePredicate(GOBLIN))), prompt)` | trigger | Skirk Drill Sergeant. One "
         "ability: the filter restricts another creature to Goblins, while `THIS_OR_ANOTHER` matches the source "
         "whatever it was;"),
        ("| `TriggeringCardConditionalEffect(CardSupertypePredicate(LEGENDARY), CreateTokenCopyOfDyingCreatureEffect("
         "CreateTokenCopyOfTargetPermanentEffect(...)))` | `Triggers.creatureDies(ANOTHER_YOU_CONTROL)` |",
         "| `CreateTokenCopyOfDyingCreatureEffect(CreateTokenCopyOfTargetPermanentEffect(...))` | "
         "`Triggers.creatureDies(PermanentHasSupertypePredicate(LEGENDARY), ANOTHER_YOU_CONTROL)` |"),
        ("| `TriggeringCardConditionalEffect(CardAllOfPredicate(non-token, not Angel), ReturnDyingCreatureToOwnerBattlefieldEffect("
         "PLUS_ONE_PLUS_ONE, 1, ANGEL, Set.of(FLYING)))` | `Triggers.creatureDies(ANOTHER_YOU_CONTROL)` |",
         "| `ReturnDyingCreatureToOwnerBattlefieldEffect(PLUS_ONE_PLUS_ONE, 1, ANGEL, Set.of(FLYING))` | "
         "`Triggers.creatureDies(PermanentAllOfPredicate(Not(IsToken), Not(HasSubtype(ANGEL))), YOU_CONTROL)` |"),
        ("| `TriggeringPermanentConditionalEffect(PermanentIsFaceDownPredicate, "
         "ReturnDyingCreatureToOwnerBattlefieldFaceDownThenTurnFaceUpEffect())` | `Triggers.creatureDies(YOU_CONTROL)` |",
         "| `ReturnDyingCreatureToOwnerBattlefieldFaceDownThenTurnFaceUpEffect()` | "
         "`Triggers.creatureDies(PermanentIsFaceDownPredicate, YOU_CONTROL)` |"),
        ("+ `TriggeringPermanentConditionalEffect(PermanentIsFaceDownPredicate, "
         "ExileDyingInstantOrSorceryAndMayCastUntilNextTurnEffect())` | `Triggers.youCastSpell()` + "
         "`Triggers.creatureDies(YOU_CONTROL)` |",
         "+ `ExileDyingInstantOrSorceryAndMayCastUntilNextTurnEffect()` | `Triggers.youCastSpell()` + "
         "`Triggers.creatureDies(PermanentIsFaceDownPredicate, YOU_CONTROL)` |"),
        ("| `TriggeringPermanentConditionalEffect(PermanentHasCountersPredicate(ANY), "
         "ExileTopCardsForEachDyingCreatureCounterMayPlayUntilNextEndStepEffect())` on `Triggers.creatureDies(YOU_CONTROL)` |",
         "| `ExileTopCardsForEachDyingCreatureCounterMayPlayUntilNextEndStepEffect()` on "
         "`Triggers.creatureDies(PermanentHasCountersPredicate(ANY), YOU_CONTROL)` |"),
        ("| `Triggers.creatureDies(ANOTHER_YOU_CONTROL)` + `TriggeringCardConditionalEffect(new CardNotPredicate(new "
         "CardSubtypePredicate(subtype)), CreateTokenEffect...)` |",
         "| `Triggers.creatureDies(new PermanentNotPredicate(new PermanentHasSubtypePredicate(subtype)), "
         "ANOTHER_YOU_CONTROL)` + `CreateTokenEffect...` |"),
        ("| `TriggeringCardConditionalEffect(CardAllOfPredicate(GREEN, Not(IsToken)), "
         "DyingCreatureControllerMaySearchLibraryForSameNameEffect())` | `Triggers.creatureDies(ANY)` |",
         "| `DyingCreatureControllerMaySearchLibraryForSameNameEffect()` | `Triggers.creatureDies(PermanentAllOfPredicate("
         "PermanentColorInPredicate(Set.of(GREEN)), Not(IsToken)), ANY)` |"),
        ("| `TriggeringCardConditionalEffect(new CardColorPredicate(GREEN), new DyingCreatureControllerDiscardsCardEffect())` "
         "| `Triggers.creatureDies(ANY)` | Bereavement. Mandatory discard routed to the DYING creature's controller (may "
         "be an opponent). The `TriggeringCardConditionalEffect` restricts to green creatures |",
         "| `new DyingCreatureControllerDiscardsCardEffect()` | `Triggers.creatureDies(new PermanentColorInPredicate("
         "Set.of(GREEN)), ANY)` | Bereavement. Mandatory discard routed to the DYING creature's controller (may be an "
         "opponent). The trigger's filter restricts it to creatures that were green as they died |"),
        ("| `Triggers.creatureDies(ANY)` + `TriggeringPermanentConditionalEffect(new PermanentHasCountersPredicate("
         "CounterType.MINUS_ONE_MINUS_ONE), new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1))` "
         "| trigger | Blowfly Infestation. `Triggers.creatureDies(ANY)` now unwraps `TriggeringPermanentConditionalEffect` "
         "against the **dying permanent** (its counters at death),",
         "| `Triggers.creatureDies(new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE), ANY)` + `new "
         "PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1)` | trigger | Blowfly Infestation. The "
         "trigger's filter is judged on the **dying permanent** as it last existed (its counters at death),"),
        ("| `Triggers.creatureDies(ANY)` and `Triggers.dies(THIS)` + `TriggeringPermanentConditionalEffect(new "
         "PermanentIsEnchantedBySourceControllerAuraPredicate(), ReturnCardFromGraveyardEffect.builder().destination("
         "HAND).filter(CREATURE and mana value at most 2).targetGraveyard(true).build())` | trigger | Dawn Evangel. "
         "`Triggers.dies(THIS)` covers the source creature dying while enchanted; the any-creature-death slot covers "
         "other creatures |",
         "| `Triggers.creatureDies(new PermanentIsEnchantedBySourceControllerAuraPredicate(), ANY)` + "
         "`ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(CREATURE and mana value at most 2)"
         ".targetGraveyard(true).build()` | trigger | Dawn Evangel. `ANY` also covers the source creature dying while "
         "enchanted, so there is no separate `Triggers.dies(THIS)` ability |"),
        ("| `Triggers.creatureDies(OPPONENT_CONTROLS)` + `TriggeringPermanentConditionalEffect(new "
         "PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE), new "
         "ReturnDyingOpponentCreatureUnderYourControlEffect())` | trigger | Necroskitter. "
         "`Triggers.creatureDies(OPPONENT_CONTROLS)` now carries the dying card + unwraps "
         "`TriggeringPermanentConditionalEffect` against the dying permanent.",
         "| `Triggers.creatureDies(new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE), "
         "OPPONENT_CONTROLS)` + `new ReturnDyingOpponentCreatureUnderYourControlEffect()` | trigger | Necroskitter. "
         "`Triggers.creatureDies(OPPONENT_CONTROLS)` carries the dying card, and its filter is judged on the dying "
         "permanent."),
        ("| `OncePerTurnTriggerEffect(CreateTokenEffect.blackZombieWithDecayed(1))` | "
         "`Triggers.nontokenCreatureDies(ANY)` | Ghoulish Procession. Decayed = can't block + sacrifice at end of combat "
         "when it attacks (baked into the token). `OncePerTurnTriggerEffect` marks the source permanent after the first "
         "fire each turn |",
         "| `CreateTokenEffect.blackZombieWithDecayed(1)` with `TriggerOptions.DEFAULT.withLimit("
         "TriggerLimit.ONCE_EACH_TURN)` | `Triggers.nontokenCreatureDies(ANY)` | Ghoulish Procession. Decayed = can't "
         "block + sacrifice at end of combat when it attacks (baked into the token). The `ONCE_EACH_TURN` limit stops "
         "the ability from triggering again that turn after it first triggers |"),
        ("| `OncePerTurnTriggerEffect(new DrawCardEffect(1))` | `Triggers.creatureDies(ANOTHER)` | Morbid Opportunist. "
         "Tokens count; source's own death does not (already left the battlefield). `OncePerTurnTriggerEffect` also "
         "collapses simultaneous multi-death into a single fire |",
         "| `new DrawCardEffect(1)` with `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)` | "
         "`Triggers.creatureDies(ANOTHER)` | Morbid Opportunist. Tokens count; source's own death does not (already "
         "left the battlefield). The `ONCE_EACH_TURN` limit also collapses simultaneous multi-death into a single "
         "fire |"),
        ("| `TriggeringPermanentConditionalEffect(PermanentHasCountersPredicate(PLUS_ONE_PLUS_ONE), "
         "PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE))` on `Triggers.creatureDies(ANOTHER_YOU_CONTROL)` +",
         "| `PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE)` on `Triggers.creatureDies(PermanentHasCountersPredicate("
         "PLUS_ONE_PLUS_ONE), ANOTHER_YOU_CONTROL)` +"),
        ("| `Triggers.creatureDies(ANY)` + `TriggeringPermanentConditionalEffect(PermanentAllOfPredicate(IsCreature, "
         "HasSupertype(LEGENDARY), HasCounters(ANY)), DrawCardForEachDyingSourceCounterEffect(null, LoseLifeEffect(2)))` "
         "| death trigger |",
         "| `Triggers.creatureDies(PermanentAllOfPredicate(IsCreature, HasSupertype(LEGENDARY), HasCounters(ANY)), ANY)` "
         "+ `DrawCardForEachDyingSourceCounterEffect(null, LoseLifeEffect(2))` | death trigger |"),
        ("| `TriggeringPermanentConditionalEffect(PermanentHasAtLeastCountersPredicate(PLUS_ONE_PLUS_ONE, 1), "
         "MayEffect(PutCounterOnTargetForEachDyingSourceCounterEffect(PLUS_ONE_PLUS_ONE)))` on "
         "`Triggers.creatureDies(YOU_CONTROL)` and `Triggers.dies(THIS)`, plus",
         "| `MayEffect(PutCounterOnTargetForEachDyingSourceCounterEffect(PLUS_ONE_PLUS_ONE))` on "
         "`Triggers.creatureDies(PermanentHasAtLeastCountersPredicate(PLUS_ONE_PLUS_ONE, 1), YOU_CONTROL)` (which "
         "includes the source itself), plus"),
        ("| `TriggeringPermanentConditionalEffect(PermanentHasCountersPredicate(ANY), MoveDyingSourceCountersToSourceEffect())` "
         "| `Triggers.creatureDies(ANOTHER_YOU_CONTROL)` |",
         "| `MoveDyingSourceCountersToSourceEffect()` | `Triggers.creatureDies(PermanentHasCountersPredicate(ANY), "
         "ANOTHER_YOU_CONTROL)` |"),
        ("+ `Triggers.creatureDies(YOU_CONTROL) TriggeringPermanentConditionalEffect(PermanentHasSubtypePredicate(ELEMENTAL), "
         "MayEffect(MillEffect(3, CONTROLLER)))`",
         "+ `Triggers.creatureDies(PermanentHasSubtypePredicate(ELEMENTAL), YOU_CONTROL) MayEffect(MillEffect(3, "
         "CONTROLLER))`"),
        ("| `OncePerTurnTriggerEffect(RevealUntilCreatureToBattlefieldOrHandByManaValueEffect())` | "
         "`Triggers.nontokenCreatureDies(YOU_CONTROL)` | Part in Friendship (HOB) |",
         "| `RevealUntilCreatureToBattlefieldOrHandByManaValueEffect()` with `TriggerOptions.DEFAULT.withLimit("
         "TriggerLimit.ONCE_EACH_TURN)` | `Triggers.nontokenCreatureDies(YOU_CONTROL)` | Part in Friendship (HOB) |"),
        ("group with `TriggeringPermanentConditionalEffect(PermanentHasCountersPredicate(ANY), "
         "MoveDyingSourceCountersToTargetCreatureEffect(true))` on `Triggers.creatureDies(THIS_OR_ANOTHER_YOU_CONTROL)` |",
         "group with `MoveDyingSourceCountersToTargetCreatureEffect(true)` on one `TriggeredAbility` "
         "(`SpellTarget.addTriggeredAbility`) with two trigger conditions, `Triggers.dies("
         "PermanentHasCountersPredicate(ANY), THIS)` and `Triggers.creatureDies(PermanentHasCountersPredicate(ANY), "
         "ANOTHER_YOU_CONTROL)`: \"if it had counters on it\" covers this creature too, which a "
         "`THIS_OR_ANOTHER_YOU_CONTROL` filter wouldn't |"),
        ("+ `addTrigger(new TriggerCondition(Triggers.creatureDies(PermanentIsAttackingPredicate(), YOU_CONTROL)), "
         "TriggerOptions.ONE_OR_MORE, OncePerTurnTriggerEffect(ReturnCardFromGraveyardEffect.builder()"
         ".destination(BATTLEFIELD).source(ALL_GRAVEYARDS).filter(CREATURE).eventCardIdsOnly(true).enterTapped(true)"
         ".underOwnersControl(true).enterWithCounter(FLYING, 1).build()))`",
         "+ `addTrigger(new TriggerCondition(Triggers.creatureDies(PermanentIsAttackingPredicate(), YOU_CONTROL)), "
         "TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.ONCE_EACH_TURN), ReturnCardFromGraveyardEffect.builder()"
         ".destination(BATTLEFIELD).source(ALL_GRAVEYARDS).filter(CREATURE).eventCardIdsOnly(true).enterTapped(true)"
         ".underOwnersControl(true).enterWithCounter(FLYING, 1).build())`"),
        ("+ `Triggers.creatureDies(YOU_CONTROL) TriggeringPermanentConditionalEffect(IsCreature + Not(IsToken) + "
         "HasCounters(OIL), ConjureDuplicatesOfDyingCreatureIntoLibraryEffect())`",
         "+ `Triggers.creatureDies(PermanentAllOfPredicate(IsCreature + Not(IsToken) + HasCounters(OIL)), YOU_CONTROL) "
         "ConjureDuplicatesOfDyingCreatureIntoLibraryEffect()`"),
        ("| `Triggers.dies(THIS)` + `Triggers.nontokenCreatureDies(ANOTHER_YOU_CONTROL)` with "
         "`TriggeringCardConditionalEffect(CardAnyOfPredicate(ZOMBIE, MUTANT), …)` | The Ghoul, Gunslinger",
         "| `Triggers.nontokenCreatureDies(PermanentAnyOfPredicate(ZOMBIE, MUTANT), THIS_OR_ANOTHER_YOU_CONTROL)` | "
         "The Ghoul, Gunslinger"),
        ("+ `Triggers.dies(Triggers.CREATURE_OR_PLANESWALKER, YOU_CONTROL) TriggeringPermanentConditionalEffect("
         "PermanentIsPlaneswalkerPredicate, same effect)`",
         "+ `Triggers.dies(PermanentAllOfPredicate(List.of(Triggers.CREATURE_OR_PLANESWALKER, "
         "PermanentIsPlaneswalkerPredicate)), YOU_CONTROL)` + the same effect"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("+ Triggers.creatureDies(YOU_CONTROL) TriggeringCardConditionalEffect(CardNotPredicate(CardSubtypePredicate("
         "ANGEL)), RegisterTransformSourceAtNextUpkeepEffect()).",
         "+ Triggers.creatureDies(PermanentNotPredicate(PermanentHasSubtypePredicate(ANGEL)), YOU_CONTROL) "
         "RegisterTransformSourceAtNextUpkeepEffect()."),
        ("| Triggers.creatureDies(YOU_CONTROL) TriggeringCardConditionalEffect(CardSubtypePredicate(SAPROLING), "
         "DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)) + TriggeringCardConditionalEffect("
         "CardSubtypePredicate(SAPROLING), GainLifeEffect(1)) — only fires when dying creature matches the predicate; "
         "multiple effects grouped into single stack entry |",
         "| Triggers.creatureDies(PermanentHasSubtypePredicate(SAPROLING), YOU_CONTROL) SequenceEffect.of("
         "DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT), GainLifeEffect(1)) — only fires when the dying "
         "creature matches the filter; both effects resolve as a single stack entry |"),
        ("`target(PlayerPredicateTargetFilter(OPPONENT)).addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), "
         "TriggeringCardConditionalEffect(CardSubtypePredicate(ZOMBIE), LoseLifeEffect(1, TARGET_PLAYER)))`",
         "`target(PlayerPredicateTargetFilter(OPPONENT)).addTrigger(Triggers.creatureDies(PermanentHasSubtypePredicate("
         "ZOMBIE), ObjectRelation.ANOTHER_YOU_CONTROL), LoseLifeEffect(1, TARGET_PLAYER))`"),
        ("\"another\" is automatic — the dying creature is off the battlefield before the watcher's trigger is checked.",
         "\"another\" is the `ANOTHER_YOU_CONTROL` relation."),
        ("| `Triggers.creatureDies(YOU_CONTROL)` TriggeringCardConditionalEffect(CardSubtypePredicate(ZOMBIE), "
         "LookAtTopCardMayRevealMatchingToHandEffect(…)) + same effect on `Triggers.dies(THIS)`. Ally slot = "
         "\"another\"; death slot = self.",
         "| `Triggers.creatureDies(PermanentHasSubtypePredicate(ZOMBIE), THIS_OR_ANOTHER_YOU_CONTROL)` "
         "LookAtTopCardMayRevealMatchingToHandEffect(…). One ability: the filter restricts \"another\", the source "
         "matches whatever it was."),
        ("| Triggers.creatureDies(OPPONENT_CONTROLS) TriggeringPermanentConditionalEffect(PermanentHasCountersPredicate("
         "MINUS_ONE_MINUS_ONE), ReturnDyingOpponentCreatureUnderYourControlEffect()) — intervening-if on the dying "
         "permanent's counters;",
         "| Triggers.creatureDies(PermanentHasCountersPredicate(MINUS_ONE_MINUS_ONE), OPPONENT_CONTROLS) "
         "ReturnDyingOpponentCreatureUnderYourControlEffect() — the filter reads the dying permanent's counters;"),
    ],
    "CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [
        ("+ Triggers.creatureDies(EQUIPPED) TriggeringCardConditionalEffect(CardSubtypePredicate(HUMAN), "
         "CreateTokenEffect(Spirit 1/1 white flying)) + equip",
         "+ Triggers.creatureDies(PermanentHasSubtypePredicate(HUMAN), EQUIPPED) CreateTokenEffect(Spirit 1/1 white "
         "flying) + equip"),
    ],
    "CARD_PATTERN_INDEX.md": [
        ("`addTrigger(new TriggerCondition(Triggers.creatureDies(new PermanentIsAttackingPredicate(), YOU_CONTROL)), "
         "TriggerOptions.ONE_OR_MORE, OncePerTurnTriggerEffect(...))`",
         "`addTrigger(new TriggerCondition(Triggers.creatureDies(new PermanentIsAttackingPredicate(), YOU_CONTROL)), "
         "TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.ONCE_EACH_TURN), ...)`"),
        ("`r/ReyhanLastOfTheAbzan.java` + `TriggeringPermanentConditionalEffect(PermanentHasAtLeastCountersPredicate("
         "PLUS_ONE_PLUS_ONE, 1), MayEffect(PutCounterOnTargetForEachDyingSourceCounterEffect(PLUS_ONE_PLUS_ONE)))`",
         "`r/ReyhanLastOfTheAbzan.java` + `Triggers.creatureDies(PermanentHasAtLeastCountersPredicate(PLUS_ONE_PLUS_ONE, "
         "1), YOU_CONTROL)` + `MayEffect(PutCounterOnTargetForEachDyingSourceCounterEffect(PLUS_ONE_PLUS_ONE))`"),
        ("`SuspectAndPerpetuallyGrantAbilityToTargetCreatureEffect()` + `TriggerCondition(Triggers.creatureDies("
         "OPPONENT_CONTROLS), GRAVEYARD)` with `TriggeringPermanentConditionalEffect(PermanentIsSuspectedPredicate, ...)`",
         "`SuspectAndPerpetuallyGrantAbilityToTargetCreatureEffect()` + `TriggerCondition(Triggers.creatureDies("
         "PermanentIsSuspectedPredicate, OPPONENT_CONTROLS), GRAVEYARD)` with "
         "`ConditionalEffect(SourceCardInGraveyard, ...)`"),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("Unwraps `TriggeringPermanentConditionalEffect` against the dying permanent — the predicate sees the dying "
         "creature's on-battlefield state incl. counters at death (e.g. Blowfly Infestation's \"if it had a -1/-1 "
         "counter on it\"). Wrap in `OncePerTurnTriggerEffect` for \"triggers only once each turn\" (Morbid "
         "Opportunist).",
         "A filter, `Triggers.creatureDies(predicate, ANY)`, is judged on the dying permanent — the predicate sees the "
         "dying creature's on-battlefield state incl. counters at death (e.g. Blowfly Infestation's \"if it had a "
         "-1/-1 counter on it\"). Use `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)` for \"triggers "
         "only once each turn\" (Morbid Opportunist)."),
        ("Used with MayEffect for Mimic Vat's imprint trigger; wrap in `OncePerTurnTriggerEffect` for \"triggers only "
         "once each turn\" (Ghoulish Procession) |",
         "Used with MayEffect for Mimic Vat's imprint trigger; use `TriggerOptions.DEFAULT.withLimit("
         "TriggerLimit.ONCE_EACH_TURN)` for \"triggers only once each turn\" (Ghoulish Procession) |"),
        ("| A creature you control dies. Supports `TriggeringCardConditionalEffect` wrapping to filter by dying creature "
         "predicates (e.g. Slimefoot only triggers for Saprolings, Requiem Angel for non-Spirits)",
         "| A creature you control dies. Takes a `PermanentPredicate` filter on the dying creature, "
         "`Triggers.creatureDies(filter, YOU_CONTROL)`, judged as it last existed (e.g. Slimefoot only triggers for "
         "Saprolings, Requiem Angel for non-Spirits)."),
        ("but fires from the controller's graveyard. Wrap in `TriggeringCardConditionalEffect` to filter the dying "
         "creature.",
         "but fires from the controller's graveyard. Filter the dying creature with "
         "`Triggers.creatureDies(filter, YOU_CONTROL)`."),
    ],
    "EFFECTS_INDEX.md": [
        ("Works with Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL) (checked in "
         "BattlefieldEntryService), Triggers.creatureDies(ANY), Triggers.creatureDies(YOU_CONTROL), "
         "Triggers.creatureDies(EQUIPPED) (checked in TriggerCollectionService),",
         "Works with Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL) (checked in "
         "BattlefieldEntryService), Triggers.creatureDies(EQUIPPED) for a condition on the card itself (Oathkeeper, "
         "Takeno's Daisho) and Triggers.creatureDies(YOU_OWN) (checked in TriggerCollectionService; other creature-death "
         "triggers take the permanent form of the predicate as the trigger's filter instead),"),
        ("Used by Champion of the Parish, Village Cannibals, Avacyn's Collar, Sanctum Seeker, Slimefoot, Requiem Angel, "
         "and Spined Sliver |",
         "Used by Champion of the Parish, Sanctum Seeker, Oathkeeper, Takeno's Daisho, and Spined Sliver |"),
        ("Wrap in `TriggeringCardConditionalEffect(<CardPredicate>, " + BAD + ")` to restrict which creatures qualify "
         "(Bereavement uses `CardColorPredicate(GREEN)`).",
         "Give the trigger a filter, `Triggers.creatureDies(<PermanentPredicate>, ANY)`, to restrict which creatures "
         "qualify (Bereavement uses `PermanentColorInPredicate(Set.of(GREEN))`)."),
        ("(may be an opponent of the source's controller). Wrap in `TriggeringCardConditionalEffect(<CardPredicate>, "
         + BAD + ")` to restrict which creatures qualify. Consumed at trigger time",
         "(may be an opponent of the source's controller). Give the trigger a filter, "
         "`Triggers.creatureDies(<PermanentPredicate>, ANY)`, to restrict which creatures qualify. Consumed at trigger "
         "time"),
        ("Used by Ghoulish Procession, Morbid Opportunist, Interface Ace, and Deathless Knight.",
         "Used by Interface Ace and Deathless Knight; creature-death triggers use "
         "`TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)` instead (Ghoulish Procession, Morbid "
         "Opportunist)."),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("Bereavement wraps it in `TriggeringCardConditionalEffect(CardColorPredicate(GREEN), …)` for \"a green creature "
         "dies\"",
         "Bereavement filters the trigger, `Triggers.creatureDies(new PermanentColorInPredicate(Set.of(GREEN)), ANY)`, "
         "for \"a green creature dies\""),
        ("(Ghoulish Procession, Morbid Opportunist, Interface Ace, Deathless Knight)",
         "(Interface Ace, Deathless Knight; creature-death triggers use "
         "`TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)` instead: Ghoulish Procession, Morbid "
         "Opportunist)"),
        ("(Ghoulish Procession, Morbid Opportunist, Interface Ace);",
         "(Interface Ace; creature-death triggers use `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)` "
         "instead: Ghoulish Procession, Morbid Opportunist);"),
    ],
}


def main():
    failed = False
    for name, replacements in REPLACEMENTS.items():
        path = DOCS + name
        text = open(path, encoding="utf-8").read()
        rewritten = 0
        for old, new in replacements:
            if new in text and old not in text:
                continue
            if text.count(old) != 1:
                print("%s: %d matches for %r" % (path, text.count(old), old[:80]))
                failed = True
                continue
            text = text.replace(old, new)
            rewritten += 1
        open(path, "w", encoding="utf-8").write(text)
        print("%s: %d of %d rewritten" % (path, rewritten, len(replacements)))
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
