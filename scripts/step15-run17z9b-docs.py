#!/usr/bin/env python3
"""Step 15, runs 17z9b and 17z10: the agent-docs rows of the abilities that read their event from the stack entry.

- `CreateFoodWhenPlayingCardFromTopOfLibraryEffect` is gone: The Fourth Doctor writes its triggers' zone and historic
  filter, the permission's use as the event condition `SourceLibraryPlayPermissionUsedThisTurn` and
  `CreateTokenEffect.ofFoodToken(1)`.
- Duelists' Convocation International's intervening "if" is `TriggeringCardManaValueIsUncrossedChosenDigit`, one ability
  of both triggers; its effect reads the triggering spell or land.
- Gutmorn, Pactbound Servant's "during your turn" is `ControllerTurn` as the event condition of one ability of both
  discard triggers; `ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffect` reads the discarding player and card the
  discard default records.
- `CopyCopiedSpellForTargetPlayerEffect` has no snapshot; it reads the entry's triggering spell. Mage Hunter's copy half
  is `LoseLifeEffect(1, TRIGGERING_PLAYER)`.

Idempotent (whole-fragment edits); fails if a doc or skill still names a removed form outside a "Gone" line.

Usage: step15-run17z9b-docs.py
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

DOCTOR_TRIGGER = ("addTrigger(List.of(Triggers.youCastSpell().withFilter(historic).castFrom(Zone.LIBRARY), "
                  "Triggers.youPlayLand().withFilter(historic).playedFrom(Zone.LIBRARY)), "
                  "TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)"
                  ".withEventCondition(new SourceLibraryPlayPermissionUsedThisTurn()), CreateTokenEffect.ofFoodToken(1))")

EDITS = {
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `CreateFoodWhenPlayingCardFromTopOfLibraryEffect(CardPredicate)` - trigger marker for creating one Food when "
         "the controller plays a matching card from the library top through the paired once-per-turn permission: one "
         "ability of both conditions, `addTrigger(List.of(Triggers.youCastSpell(), Triggers.youPlayLand()), "
         "TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), effect)` (The Fourth Doctor, WHO 2).",
         "- `CreateFoodWhenPlayingCardFromTopOfLibraryEffect` — **Gone** (effect-slot-refactor.md step 15, run 17z9b): "
         "\"when you do, create a Food token\" of the paired permission is one ability of both plays, the zone and "
         "filter on its triggers and the permission's use its event condition: `" + DOCTOR_TRIGGER + "` (The Fourth "
         "Doctor, WHO 2)."),
        ("draw one card, cross the first matching uncrossed digit, and win after all ten positions are crossed "
         "(MB2 293/529).",
         "draw one card, cross the first matching uncrossed digit, and win after all ten positions are crossed "
         "(MB2 293/529). One ability of `List.of(Triggers.youPlayLand(), Triggers.youCastSpell().anySpell())` with the "
         "intervening \"if\" `TriggeringCardManaValueIsUncrossedChosenDigit()`; the effect reads the mana value of the "
         "triggering spell (as it was on the stack) or land from the stack entry."),
        ("who receives a fresh-identity duplicate with perpetual any-color casting permission. Used by Gutmorn, "
         "Pactbound Servant (YMID 28).",
         "who receives a fresh-identity duplicate with perpetual any-color casting permission. It reads the discarding "
         "player (`StackEntry.getTriggeringPlayerId()`) and the card (`getTriggeringCards()`) the discard default records. "
         "Used by Gutmorn, Pactbound Servant (YMID 28): one ability of `List.of(Triggers.youDiscardCard(), "
         "Triggers.opponentDiscardsCard())` with `TriggerOptions.DEFAULT.withEventCondition(new ControllerTurn())` "
         "(\"during your turn\")."),
        ("with that player choosing new targets for the copy; the trigger collector supplies a snapshot of the copied "
         "spell (Parnesse, the Subtle Brush)",
         "with that player choosing new targets for the copy; \"that spell\" is the stack entry's triggering spell, the "
         "copy the event was about, as it is on the stack or as it last existed there (Parnesse, the Subtle Brush)"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `CreateFoodWhenPlayingCardFromTopOfLibraryEffect` | `(CardPredicate filter)` | trigger marker that creates "
         "one Food when a matching card is played from the controller's library top through the paired permission | "
         "The Fourth Doctor (`WHO` 2) |",
         "| `CreateFoodWhenPlayingCardFromTopOfLibraryEffect` | — | **Gone** (effect-slot-refactor.md step 15, run "
         "17z9b): `" + DOCTOR_TRIGGER + "` | The Fourth Doctor (`WHO` 2) |"),
        ("matching controller land plays and spell casts cross one uncrossed digit, draw a card, and win after the "
         "tenth position is crossed. |",
         "matching controller land plays and spell casts cross one uncrossed digit, draw a card, and win after the "
         "tenth position is crossed. One ability of both plays with the intervening \"if\" "
         "`TriggeringCardManaValueIsUncrossedChosenDigit()`; the effect reads the triggering spell or land. |"),
        ("| `ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffect` | `()` | The player who discarded the triggering "
         "card chooses another player, who receives a fresh-identity duplicate with perpetual any-color-mana casting "
         "permission. |",
         "| `ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffect` | `()` | The player who discarded the triggering "
         "card chooses another player, who receives a fresh-identity duplicate with perpetual any-color-mana casting "
         "permission. Reads the discarding player and card the discard default records (`getTriggeringPlayerId()`, "
         "`getTriggeringCards()`); \"during your turn\" is the ability's `ControllerTurn` event condition. |"),
        ("| `CopyCopiedSpellForTargetPlayerEffect` | `()` | copies the spell snapshot that caused a copy trigger "
         "(`Triggers.youCopySpell()`) for the chosen player target,",
         "| `CopyCopiedSpellForTargetPlayerEffect` | `()` | copies the stack entry's triggering spell, the copy that "
         "caused a copy trigger (`Triggers.youCopySpell()`), as it is on the stack or as it last existed there, for the "
         "chosen player target,"),
        ("| `TriggeringSpellMatches` (condition) | `(StackEntryPredicate filter)` |",
         "| `TriggeringCardManaValueIsUncrossedChosenDigit` (condition) | `()` | Duelists' Convocation International's "
         "intervening \"if\" (`TriggerOptions.DEFAULT.withInterveningIf(...)`): the mana value of the triggering spell "
         "as it is on the stack (X the number chosen, 0 cast face down), else of the triggering card (the land played), "
         "is one of the source's chosen digits not crossed out. The cast and land-play collectors judge it as the spell "
         "is cast or the land played, and again as the ability resolves |\n"
         "| `SourceLibraryPlayPermissionUsedThisTurn` (condition) | `()` | the source's "
         "`PlayLandOrCastSpellFromTopOfLibraryOncePerTurnEffect` permission was used this turn: The Fourth Doctor's "
         "\"when you do\" as the event condition (`withEventCondition`) of its cast and land-play triggers, with "
         "`TriggerLimit.ONCE_EACH_TURN` |\n"
         "| `TriggeringSpellMatches` (condition) | `(StackEntryPredicate filter)` |"),
    ],
    "ARCHITECTURE.md": [
        ("so a copy excludes itself rather than the card it copied.\n",
         "so a copy excludes itself rather than the card it copied. A discard trigger's \"they\" and \"that card\" "
         "(Gutmorn, Pactbound Servant) are the discarding player and the card discarded, which the discard default "
         "records on its entry (`StackEntry.triggeringPlayerId`, `setTriggeringCards`); the land-play default records "
         "the land played as the triggering card the same way.\n"),
        ("has the spell as it last existed on the stack as its triggering spell.\n",
         "has the spell as it last existed on the stack as its triggering spell. A spell-copy trigger's \"that spell\" "
         "is the copy the event was about: the copy collector records it on its entry and gives its target choice the "
         "copy's card id (`triggeringCardId`), from which the entry built takes it as its triggering spell (Parnesse, "
         "the Subtle Brush's `CopyCopiedSpellForTargetPlayerEffect`).\n"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("`addTrigger(List.of(Triggers.youCastSpell(), Triggers.youPlayLand()), "
         "TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), "
         "CreateFoodWhenPlayingCardFromTopOfLibraryEffect(CardIsHistoricPredicate()))`",
         "`" + DOCTOR_TRIGGER + "` (`historic` = `CardIsHistoricPredicate()`)"),
        ("`Triggers.enters(ObjectRelation.THIS) InitializeDuelistsConvocationInternationalEffect()` + "
         "`Triggers.youPlayLand() DuelistsConvocationInternationalTriggerEffect()` + `Triggers.youCastSpell().anySpell()` "
         "+ `DuelistsConvocationInternationalTriggerEffect()`; `Permanent` stores the chosen digits and crossed positions",
         "`Triggers.enters(ObjectRelation.THIS) InitializeDuelistsConvocationInternationalEffect()` + "
         "`addTrigger(List.of(Triggers.youPlayLand(), Triggers.youCastSpell().anySpell()), "
         "TriggerOptions.DEFAULT.withInterveningIf(new TriggeringCardManaValueIsUncrossedChosenDigit()), "
         "DuelistsConvocationInternationalTriggerEffect())`; `Permanent` stores the chosen digits and crossed positions"),
        ("The copy trigger includes permanent-spell copies, snapshots the copied spell, and makes its opponent target "
         "optional.",
         "The copy trigger includes permanent-spell copies, reads the copy as the entry's triggering spell, and makes its "
         "opponent target optional."),
        ("`Triggers.enters(ObjectRelation.THIS) EachPlayerDiscardsNonlandCardEffect()` + `Triggers.youDiscardCard()` and "
         "`Triggers.opponentDiscardsCard() ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffect()` | "
         "`Triggers.enters(ObjectRelation.THIS)` + discard triggers | Gutmorn, Pactbound Servant (YMID 28); the ETB skips "
         "players with no nonland card, and each discard trigger is limited to the active player's turn |",
         "`Triggers.enters(ObjectRelation.THIS) EachPlayerDiscardsNonlandCardEffect()` + "
         "`addTrigger(List.of(Triggers.youDiscardCard(), Triggers.opponentDiscardsCard()), "
         "TriggerOptions.DEFAULT.withEventCondition(new ControllerTurn()), "
         "ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffect())` | `Triggers.enters(ObjectRelation.THIS)` + "
         "discard triggers | Gutmorn, Pactbound Servant (YMID 28); the ETB skips players with no nonland card, and "
         "\"during your turn\" is the discard ability's event condition |"),
    ],
}

GONE = ["CreateFoodWhenPlayingCardFromTopOfLibraryEffect(", "CopiedSpellReferencingEffect", "snapshotCopiedSpell",
        "snapshots the copied spell", "copies the spell snapshot that caused"]


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if text.count(old) != 1:
                raise SystemExit(f"{name}: expected exactly one occurrence of: {old[:100]}")
            text = text.replace(old, new)
        path.write_text(text)
        print(f"{name}: up to date")
    stale = []
    for path in list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md")):
        for number, line in enumerate(path.read_text().splitlines(), 1):
            if "Gone" not in line and any(form in line for form in GONE):
                stale.append(f"{path.relative_to(ROOT)}:{number}")
    if stale:
        raise SystemExit("a removed form is still named: " + ", ".join(stale))


if __name__ == "__main__":
    sys.exit(main())
