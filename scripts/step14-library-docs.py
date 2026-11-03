#!/usr/bin/env python3
"""Rewrites the agent-docs lines that teach the library, draw and discard trigger wrappers (effect-slot-refactor.md
step 14, run 11): the converted rows show the trigger, filter and options, and the wrapper classes' entries say not to
author them. Exact replacements; stops on one that doesn't match.

Usage: python3 scripts/step14-library-docs.py [--dry-run]
"""
import sys

DONT = ("Don't author it: the legacy view (`TriggeredAbility.legacyEffects()`) builds it for the collectors from {}.")

R = {
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md": [
        ("| `CyclingTriggerEffect(new GainLifeEffect(1))` | `Triggers.youDiscardCard()` | Drannith Healer (`IKO`); the "
         "cycling-only wrapper distinguishes cycling from ordinary discards,",
         "| `GainLifeEffect(1)` | `Triggers.youCycleCard()` | Drannith Healer (`IKO`); the \"you cycle a card\" trigger "
         "distinguishes cycling from ordinary discards,"),
        ("| `Triggers.youDrawCard()` `FirstOrSecondCardDrawTriggerEffect(PutCountersOnSelfEffect(INGENUITY))` +",
         "| `addTrigger(List.of(Triggers.youDrawNthCardEachTurn(1), Triggers.youDrawSecondCardEachTurn()), "
         "PutCountersOnSelfEffect(INGENUITY))` +"),
        ("Lady Octopus, Inspired Inventor (SPM). The draw marker fires only for draw counts 1 and 2;",
         "Lady Octopus, Inspired Inventor (SPM). One ability with two trigger conditions, so it triggers only for draw "
         "counts 1 and 2;"),
        ("| `TriggeringCardConditionalEffect(CardTypePredicate(CREATURE), CreateTokenEffect.blackZombie(1))` / "
         "`TriggeringCardConditionalEffect(CardTypePredicate(LAND), AwardManaEffect(BLACK, 2))` / "
         "`TriggeringCardConditionalEffect(CardAllOfPredicate(Not(CREATURE), Not(LAND)), DrawCardEffect())` | "
         "Triggers.opponentDiscardsCard() | Waste Not. `TriggerCollectionService.checkDiscardTriggers` unwraps "
         "`TriggeringCardConditionalEffect` against the discarded card, so the card-type gate lives in the predicate;",
         "| `CreateTokenEffect.blackZombie(1)` / `AwardManaEffect(BLACK, 2)` / `DrawCardEffect()` | "
         "`Triggers.opponentDiscardsCard().withFilter(CardTypePredicate(CREATURE))` / `.withFilter(CardTypePredicate(LAND))` "
         "/ `.withFilter(CardAllOfPredicate(Not(CREATURE), Not(LAND)))` | Waste Not. The trigger's filter judges the "
         "discarded card (`TriggerCollectionService.checkDiscardTriggers` reads it from the legacy view);"),
        ("| `DrawDiscardAndConniveEffect()` + `OncePerTurnTriggerEffect(SeekCardSharingCardTypeWithDiscardedCardsEffect())` "
         "| Triggers.enters(ObjectRelation.THIS) + Triggers.youDiscardOneOrMoreCards() |",
         "| `DrawDiscardAndConniveEffect()` + `SeekCardSharingCardTypeWithDiscardedCardsEffect()` with "
         "`TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)` | Triggers.enters(ObjectRelation.THIS) + "
         "Triggers.youDiscardOneOrMoreCards() |"),
        ("Second ability = `Triggers.opponentCausesYouToDiscardThisCard()` + `ConditionalEffect(new ControlsPermanent(new "
         "PermanentHasSubtypePredicate(CardSubtype.PLAINS)), CreateTokenEffect(...))` (intervening-if) |",
         "Second ability = `Triggers.opponentCausesYouToDiscardThisCard()` + `TriggerOptions.DEFAULT.withInterveningIf(new "
         "ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.PLAINS)))` + `CreateTokenEffect(...)` |"),
        ("`target(opponent).addTrigger(Triggers.youDrawCard(), ConditionalEffect(ControllerIsMonarch, LoseLifeEffect(2, "
         "TARGET_PLAYER)))`",
         "`target(opponent).addTrigger(Triggers.youDrawCard(), TriggerOptions.DEFAULT.withInterveningIf(new "
         "ControllerIsMonarch()), LoseLifeEffect(2, TARGET_PLAYER))`"),
        ("| `CyclingTriggerEffect(new MayEffect(new BecomeCopyOfTargetPermanentUntilYourNextTurnEffect(new "
         "PermanentNotPredicate(new PermanentIsSourceCardPredicate()), Triggers.youDiscardCard()), prompt))` | "
         "Triggers.youDiscardCard() | Cycling-only discard trigger;",
         "| `MayEffect(new BecomeCopyOfTargetPermanentUntilYourNextTurnEffect(new PermanentNotPredicate(new "
         "PermanentIsSourceCardPredicate()), Triggers.youCycleCard()), prompt)` | Triggers.youCycleCard() | \"You cycle a "
         "card\" trigger;"),
        ("| Erudite Wizard. The dedicated draw slot fires exactly when the controller's per-turn draw count reaches two, "
         "including cards drawn outside the draw step |",
         "| Erudite Wizard. The trigger counts the controller's draws this turn (`LibraryTrigger.nth()`), so it fires "
         "exactly when that count reaches two, including cards drawn outside the draw step |"),
        ("| `NthCardDrawTriggerEffect(2, MillEffect(3, MillRecipient.TARGET_PLAYER))` | `Triggers.youDrawCard()` | "
         "Master's Councillors (HOB 47) |",
         "| `MillEffect(3, MillRecipient.TARGET_PLAYER)` | `Triggers.youDrawSecondCardEachTurn()` | Master's Councillors "
         "(HOB 47) |"),
        ("| `OpponentCausedDiscardTriggerEffect(SequenceEffect.of(GainLifeEffect(2), MayEffect(DrawCardEffect(1), \"Draw a "
         "card?\")))` | Triggers.youDiscardCard() | Spiritual Focus (MMQ). The trigger wrapper checks "
         "`discardCausedByOpponent`;",
         "| `SequenceEffect.of(GainLifeEffect(2), MayEffect(DrawCardEffect(1), \"Draw a card?\"))` | "
         "Triggers.opponentCausesYouToDiscardCard() | Spiritual Focus (MMQ). The collector checks "
         "`discardCausedByOpponent`;"),
        ("`Triggers.opponentDrawsCard() ExceptFirstDrawStepTriggerEffect(SequenceEffect.of(DealDamageToAnyTargetEffect(1), "
         "AmassGoblinsEffect(1, ORC)))`",
         "`Triggers.opponentDrawsCardExceptFirstInDrawStep() SequenceEffect.of(DealDamageToAnyTargetEffect(1), "
         "AmassGoblinsEffect(1, ORC))`"),
        ("`Triggers.opponentDrawsCard() NthCardDrawTriggerEffect(2, DrawCardEffect(2))` | trigger | The Unagi of Kyoshi "
         "Island",
         "`Triggers.opponentDrawsSecondCardEachTurn() DrawCardEffect(2)` | trigger | The Unagi of Kyoshi Island"),
        ("| `Triggers.youDrawCard()` and `Triggers.opponentDrawsCard()` each use `NthCardDrawTriggerEffect(2, "
         "ExileTopCardsToSourceEffect(...))`;",
         "| `Triggers.youDrawSecondCardEachTurn()` and `Triggers.opponentDrawsSecondCardEachTurn()` each with "
         "`ExileTopCardsToSourceEffect(...)`;"),
        ("| `Triggers.youDiscardCard() TriggeringCardConditionalEffect(CardTypePredicate(CREATURE), "
         "MayPayManaEffect(\"{2}{U}\", CreateTokenCopyOfCardEffect.fromTriggeringCard(CreateTokenCopyOfTargetPermanentEffect"
         "(...))))` |",
         "| `Triggers.youDiscardCard().withFilter(CardTypePredicate(CREATURE))` + `MayPayManaEffect(\"{2}{U}\", "
         "CreateTokenCopyOfCardEffect.fromTriggeringCard(CreateTokenCopyOfTargetPermanentEffect(...)))` |"),
    ],
    "agent-docs/EFFECTS_QUICK_REFERENCE.md": [
        ("- `CyclingTriggerEffect(CardEffect wrapped)` — `Triggers.youDiscardCard()` trigger-only wrapper that queues "
         "`wrapped` only for a discard caused by activating a cycling ability; use for cycling-only triggers such as "
         "Drannith Healer (`IKO`)",
         "- `CyclingTriggerEffect(CardEffect wrapped)` — the \"you discard a card\" collector's cycling-only wrapper. "
         + DONT.format("`Triggers.youCycleCard()` (Drannith Healer, `IKO`)")),
        ("- `OncePerTurnTriggerEffect.firstSurveil(effect)` — `Triggers.youSurveil()` only: triggers for the controller's "
         "first surveil each turn, counting surveils made before the source entered "
         "(`TriggerContext.Surveil.firstThisTurn`). Whispering Snitch.",
         "- `OncePerTurnTriggerEffect.firstSurveil(effect)` — the surveil collector's \"first surveil each turn\" wrapper, "
         "counting surveils made before the source entered (`TriggerContext.Surveil.firstThisTurn`). "
         + DONT.format("`Triggers.youSurveil().firstTimeEachTurn()` (Whispering Snitch); `firstCycling`, "
                       "`firstNonlandDiscard` and `firstDiscardEvent` likewise from `youCycleCard()`, "
                       "`youDiscardCard().withFilter(nonland)` and `youDiscardOneOrMoreCards()` with "
                       "`.firstTimeEachTurn()`")),
        ("- `FirstOrSecondCardDrawTriggerEffect(CardEffect)` — `Triggers.youDrawCard()` trigger marker that unwraps its "
         "payload when the controller draws their first or second card of the turn (Lady Octopus, Inspired Inventor)",
         "- `FirstOrSecondCardDrawTriggerEffect(CardEffect)` — draw collector marker for the first or second card of the "
         "turn. " + DONT.format("`List.of(Triggers.youDrawNthCardEachTurn(1), Triggers.youDrawSecondCardEachTurn())` "
                                "(Lady Octopus, Inspired Inventor); `NthCardDrawTriggerEffect` likewise from "
                                "`youDrawNthCardEachTurn(n)` / `opponentDrawsNthCardEachTurn(n)`")),
        ("- `ExceptFirstDrawStepTriggerEffect(CardEffect)` — draw trigger marker that suppresses its payload for the first "
         "card drawn in the active player's draw step, but fires for additional draws (Orcish Bowmasters)",
         "- `ExceptFirstDrawStepTriggerEffect(CardEffect)` — draw collector marker that skips the first card drawn in the "
         "drawing player's draw step. " + DONT.format("`Triggers.opponentDrawsCardExceptFirstInDrawStep()` (Orcish "
                                                      "Bowmasters)")),
        ("- `FirstDrawStepDrawOnlyTriggerEffect(CardEffect)` — draw trigger marker that applies its payload only to the "
         "first card drawn in the active player's draw step (Wiretapping, Triggers.youDrawCard())",
         "- `FirstDrawStepDrawOnlyTriggerEffect(CardEffect)` — draw collector marker for only the first card drawn in the "
         "drawing player's draw step. " + DONT.format("`Triggers.youDrawFirstCardInDrawStep()` (Wiretapping)")),
        ("`OpponentCausedDiscardTriggerEffect(CardEffect)` — trigger-only `Triggers.youDiscardCard()` wrapper; fires only "
         "when an opponent's spell or ability caused the controller's discard, then queues the wrapped effect as one "
         "triggered ability (Spiritual Focus).",
         "`OpponentCausedDiscardTriggerEffect(CardEffect)` — the \"you discard a card\" collector's wrapper for a discard an "
         "opponent's spell or ability caused. " + DONT.format("`Triggers.opponentCausesYouToDiscardCard()` (Spiritual "
                                                              "Focus)")),
    ],
    "agent-docs/EFFECTS_INDEX.md": [
        ("| trigger-only `Triggers.youDiscardCard()` wrapper that queues `wrapped` only when the discard was caused by "
         "activating a cycling ability; keeps cycling-only triggers distinct from ordinary discard triggers. Used by "
         "Drannith Healer (`IKO`) |",
         "| the \"you discard a card\" collector's cycling-only wrapper. "
         + DONT.format("`Triggers.youCycleCard()` (Drannith Healer, `IKO`)") + " |"),
        ("| `Triggers.youDrawCard()` trigger marker that unwraps `resolvedEffect` when the controller's draw count for the "
         "turn is 1 or 2. Lady Octopus, Inspired Inventor |",
         "| draw collector marker for the controller's draw counts 1 and 2. "
         + DONT.format("`List.of(Triggers.youDrawNthCardEachTurn(1), Triggers.youDrawSecondCardEachTurn())` (Lady "
                       "Octopus, Inspired Inventor); `NthCardDrawTriggerEffect` likewise from `youDrawNthCardEachTurn(n)` "
                       "/ `opponentDrawsNthCardEachTurn(n)`") + " |"),
        ("| draw trigger marker that suppresses `resolvedEffect` for the first card drawn in the active player's draw step, "
         "but fires for additional draws. Orcish Bowmasters |",
         "| draw collector marker that skips the first card drawn in the drawing player's draw step. "
         + DONT.format("`Triggers.opponentDrawsCardExceptFirstInDrawStep()` (Orcish Bowmasters)") + " |"),
        ("| draw trigger marker that applies `resolvedEffect` only to the first card drawn in the active player's draw "
         "step. Wiretapping |",
         "| draw collector marker for only the first card drawn in the drawing player's draw step. "
         + DONT.format("`Triggers.youDrawFirstCardInDrawStep()` (Wiretapping)") + " |"),
        ("| Trigger-only wrapper for `Triggers.youDiscardCard()`: fires only when an opponent's spell or ability caused the "
         "controller's discard, then queues the wrapped effect as one triggered ability. Use `SequenceEffect` when the "
         "trigger has a mandatory step followed by an optional step, such as Spiritual Focus's life gain followed by its "
         "optional draw |",
         "| the \"you discard a card\" collector's wrapper for a discard an opponent's spell or ability caused. "
         + DONT.format("`Triggers.opponentCausesYouToDiscardCard()` (Spiritual Focus, whose `SequenceEffect` keeps the "
                       "life gain before the optional draw)") + " |"),
        ("`OncePerTurnTriggerEffect.firstCycling(wrapped)` triggers only for the controller's first cycling event each "
         "turn, including cycling before the source entered the battlefield (Valiant Rescuer). It is checked against the "
         "cycling counter in the discard trigger collector rather than a per-source marker.",
         "`OncePerTurnTriggerEffect.firstCycling(wrapped)` triggers only for the controller's first cycling event each "
         "turn, including cycling before the source entered the battlefield. It is checked against the cycling counter "
         "in the discard trigger collector rather than a per-source marker. "
         + DONT.format("`Triggers.youCycleCard().firstTimeEachTurn()` (Valiant Rescuer)")),
    ],
    "agent-docs/CARD_PATTERN_INDEX.md": [
        ("`Triggers.youDiscardCard() TriggeringCardConditionalEffect(CardNotPredicate(LAND), "
         "OncePerTurnTriggerEffect.firstNonlandDiscard(...))`",
         "`Triggers.youDiscardCard().withFilter(CardNotPredicate(LAND)).firstTimeEachTurn()`"),
        ("`h/HashatonScarabsFist.java` + `Triggers.youDiscardCard() TriggeringCardConditionalEffect(CardTypePredicate("
         "CREATURE), MayPayManaEffect(\"{2}{U}\", CreateTokenCopyOfCardEffect.fromTriggeringCard(...)))` |",
         "`h/HashatonScarabsFist.java` + `Triggers.youDiscardCard().withFilter(CardTypePredicate(CREATURE))` + "
         "`MayPayManaEffect(\"{2}{U}\", CreateTokenCopyOfCardEffect.fromTriggeringCard(...))` |"),
    ],
    "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| Triggers.opponentDrawsCard() ExceptFirstDrawStepTriggerEffect(SequenceEffect.of(DealDamageToAnyTargetEffect(1), "
         "AmassGoblinsEffect(1, ORC))) —",
         "| Triggers.opponentDrawsCardExceptFirstInDrawStep() SequenceEffect.of(DealDamageToAnyTargetEffect(1), "
         "AmassGoblinsEffect(1, ORC)) —"),
        ("| Triggers.opponentDrawsCard() NthCardDrawTriggerEffect(2, DrawCardEffect(2)) — fires once when each opponent's "
         "per-turn draw count reaches two |",
         "| Triggers.opponentDrawsSecondCardEachTurn() DrawCardEffect(2) — fires once when each opponent's per-turn draw "
         "count reaches two |"),
        ("| Three `Triggers.opponentDiscardsCard()` effects, each wrapped in `TriggeringCardConditionalEffect(CardPredicate, "
         "wrapped)`: `CardTypePredicate(CREATURE)` → `CreateTokenEffect.blackZombie(1)`, `CardTypePredicate(LAND)` → "
         "`AwardManaEffect(BLACK, 2)`, `CardAllOfPredicate(Not(CREATURE), Not(LAND))` → `DrawCardEffect()`. "
         "`TriggerCollectionService.checkDiscardTriggers` unwraps the conditional against the discarded card;",
         "| Three `Triggers.opponentDiscardsCard().withFilter(CardPredicate)` abilities: `CardTypePredicate(CREATURE)` → "
         "`CreateTokenEffect.blackZombie(1)`, `CardTypePredicate(LAND)` → `AwardManaEffect(BLACK, 2)`, "
         "`CardAllOfPredicate(Not(CREATURE), Not(LAND))` → `DrawCardEffect()`. "
         "`TriggerCollectionService.checkDiscardTriggers` judges the filter against the discarded card;"),
        ("| `Triggers.youDiscardCard() TriggeringCardConditionalEffect(CardTypePredicate(CREATURE), "
         "MayPayManaEffect(\"{2}{U}\", CreateTokenCopyOfCardEffect.fromTriggeringCard(...)))`;",
         "| `Triggers.youDiscardCard().withFilter(CardTypePredicate(CREATURE))` + `MayPayManaEffect(\"{2}{U}\", "
         "CreateTokenCopyOfCardEffect.fromTriggeringCard(...))`;"),
    ],
    "agent-docs/ACTIVATED_ABILITY_GUIDE.md": [
        ("Blood Operative uses `ConditionalEffect(SourceCardInGraveyard, MayPayManaEffect(\"{0}\", 3, "
         "ReturnSourceCardFromGraveyardToOwnerHandEffect(), ...))` |",
         "Blood Operative uses `TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInGraveyard())` with "
         "`MayPayManaEffect(\"{0}\", 3, ReturnSourceCardFromGraveyardToOwnerHandEffect(), ...)` |"),
        ("Use `NthCardDrawTriggerEffect` for an exact per-turn draw number, such as \"your second card each turn\". |",
         "For an exact per-turn draw number use `Triggers.youDrawSecondCardEachTurn()` / "
         "`youDrawNthCardEachTurn(n)`; for \"your first card during each of your draw steps\" "
         "`youDrawFirstCardInDrawStep()`. |"),
        ("`NthCardDrawTriggerEffect` narrows this to an opponent's exact per-turn draw number. |",
         "`Triggers.opponentDrawsSecondCardEachTurn()` / `opponentDrawsNthCardEachTurn(n)` narrow it to an opponent's "
         "exact per-turn draw number, `opponentDrawsCardExceptFirstInDrawStep()` skips the first card of their draw "
         "step. |"),
    ],
    "agent-docs/TRIGGER_SLOT_TARGETING.md": [
        ("| `DrawService.checkOpponentDrawTriggers` scans each non-drawing player's graveyard on that player's second draw "
         "of the turn |",
         "| `DrawService.checkOpponentDrawTriggers` scans each non-drawing player's graveyard on every draw "
         "(`LibraryWatch.GRAVEYARD_ON_OPPONENT_DRAWS`); the legacy view's `NthCardDrawTriggerEffect` fires it on the "
         "second |"),
        ("`Triggers.youDrawSecondCardEachTurn()` is checked by `DrawService.checkControllerDrawTriggers` after\n"
         "the controller's per-turn draw count reaches exactly two. It uses the same stack and any-target\n"
         "handling as `Triggers.youDrawCard()`, but does not fire on later draws in the same turn.",
         "`Triggers.youDrawSecondCardEachTurn()` (and `youDrawNthCardEachTurn(n)`, the opponent forms and\n"
         "\"first or second\" as two conditions of one ability) is read by the `Triggers.youDrawCard()`\n"
         "collector in `DrawService.checkControllerDrawTriggers`: the legacy view hands it an\n"
         "`NthCardDrawTriggerEffect`, so the ability fires only when the controller's per-turn draw count\n"
         "reaches exactly that number, with the same stack and any-target handling."),
    ],
    "agent-docs/CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("`Triggers.opponentDrawsCard() NthCardDrawTriggerEffect(2, DrawCardEffect)`",
         "`Triggers.opponentDrawsSecondCardEachTurn()` + `DrawCardEffect`"),
    ],
}


def main():
    dry = "--dry-run" in sys.argv
    count = 0
    for path, pairs in R.items():
        src = open(path).read()
        for old, new in pairs:
            if src.count(old) != 1:
                print("STOP %s: %d matches for %r" % (path, src.count(old), old[:80]))
                return 1
            src = src.replace(old, new)
            count += 1
        if not dry:
            open(path, "w").write(src)
    print("rewrote %d passages" % count)
    return 0


if __name__ == "__main__":
    sys.exit(main())
