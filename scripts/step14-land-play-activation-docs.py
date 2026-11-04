#!/usr/bin/env python3
"""Step 14, run 16c of effect-slot-refactor.md: the agent-docs passages about the land-play and activation wrappers.

The land-play and activation collectors read the abilities themselves now, so the engine no longer builds
LandPlayFromExileTriggerEffect, PlayFromOutsideHandTriggerEffect (for a printed ability) or the
Triggering…AbilityConditionalEffect wrappers, and the deleted records' rows go. The authoring guidance they carried
("whenever you activate a loyalty ability", "with {T} in its activation cost") moves to the activation-kind line of
EFFECTS_QUICK_REFERENCE.md. Each edit is an exact replacement; running the script again changes nothing.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent

INDEX_ROWS_REMOVED = [
    "| `TriggeringPowerUpAbilityConditionalEffect` |",
    "| `TriggeringOutlastAbilityConditionalEffect` |",
    "| `TriggeringTapAbilityConditionalEffect` |",
    "| `TriggeringNonTapAbilityConditionalEffect` |",
    "| `TriggeringLoyaltyAbilityConditionalEffect` |",
    "| `LandPlayFromExileTriggerEffect` |",
]
QUICK_LINES_REMOVED = [
    "- `TriggeringTapAbilityConditionalEffect(CardEffect)` —",
    "- `TriggeringNonTapAbilityConditionalEffect(CardEffect)` —",
    "- `TriggeringLoyaltyAbilityConditionalEffect(CardEffect)` —",
    "- `LandPlayFromExileTriggerEffect` —",
]
OUTSIDE_HAND_NEW = (
    "**Don't author** in a printed ability: \"whenever you cast a spell / play a land from anywhere other than your "
    "hand\" is `addTrigger(Triggers.youCastSpell().notCastFrom(Zone.HAND), effects...)` / "
    "`addTrigger(Triggers.youPlayLand().notPlayedFrom(Zone.HAND), effects...)`, and \"from exile\" "
    "`.castFrom(Zone.EXILE)` / `.playedFrom(Zone.EXILE)` (a target as a target group); the cast and land-play "
    "collectors read such an ability itself. It is authored only as the effect of Apple of Eden, Isu Relic's "
    "registered \"this way\" abilities, bound to the source as they register."
)
OUTSIDE_HAND_OLD = (
    "**Don't author** (effect-slot-refactor.md step 14, run 6c): \"whenever you cast a spell / play a land from "
    "anywhere other than your hand\" is `addTrigger(Triggers.youCastSpell().notCastFrom(Zone.HAND), effects...)` / "
    "`addTrigger(Triggers.youPlayLand().notPlayedFrom(Zone.HAND), effects...)` (a target as a target group). The "
    "engine builds this record for the land-play collectors (`TriggeredAbility.legacyEffects()`); it stays authored "
    "only in Apple of Eden, Isu Relic's registered \"this way\" abilities."
)
KIND_OLD = (
    "- “Whenever you activate a ninjutsu / power-up / boast / outlast / loyalty ability” is\n"
    "  `Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.NINJUTSU)` (`POWER_UP`, `BOAST`,\n"
    "  `OUTLAST`, `LOYALTY`; Satoru Umezawa, Marvel Boy, Noh-Varr), not the `Triggering…AbilityConditionalEffect`\n"
    "  wrappers, which only the legacy view still builds\n"
)
KIND_NEW = (
    "- “Whenever you activate a ninjutsu / power-up / boast / outlast / loyalty ability” is\n"
    "  `Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.NINJUTSU)` (`POWER_UP`, `BOAST`,\n"
    "  `OUTLAST`, `LOYALTY`; Satoru Umezawa, Marvel Boy, Noh-Varr), “with / without {T} in its activation cost”\n"
    "  `.withKind(AbilityTrigger.AbilityKind.WITH_TAP_COST)` / `WITHOUT_TAP_COST` (Imprison, Artifact Possession)\n"
    "  and “an ability of [a permanent]” `.withSource(permanentPredicate)` (Ceaseless Searblades, “of a Chandra\n"
    "  planeswalker” Keral Keep Disciples, “this creature's outlast ability” `new PermanentIsSourceCardPredicate()`,\n"
    "  Herald of Anafenza). The activation collectors judge the kind, the source and the ability's options\n"
    "  themselves (`TriggerCollectionService.activationEffects`)\n"
)


def drop_lines(path, prefixes):
    text = path.read_text()
    lines = text.split("\n")
    kept = [line for line in lines if not any(line.startswith(prefix) for prefix in prefixes)]
    if len(kept) != len(lines):
        path.write_text("\n".join(kept))
    return len(lines) - len(kept)


def replace(path, old, new):
    text = path.read_text()
    if new in text:
        return 0
    if text.count(old) != 1:
        sys.exit(f"{path}: expected one occurrence of {old[:60]!r}")
    path.write_text(text.replace(old, new))
    return 1


def main():
    index = ROOT / "agent-docs" / "EFFECTS_INDEX.md"
    quick = ROOT / "agent-docs" / "EFFECTS_QUICK_REFERENCE.md"
    changes = drop_lines(index, INDEX_ROWS_REMOVED)
    changes += drop_lines(quick, QUICK_LINES_REMOVED)
    changes += replace(index, OUTSIDE_HAND_OLD, OUTSIDE_HAND_NEW)
    changes += replace(quick, OUTSIDE_HAND_OLD, OUTSIDE_HAND_NEW)
    changes += replace(quick, KIND_OLD, KIND_NEW)
    print(f"{changes} change(s)")


if __name__ == "__main__":
    main()
