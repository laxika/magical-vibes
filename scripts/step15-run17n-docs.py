#!/usr/bin/env python3
"""Step 15, run 17n: the agent docs show the opponent-land and chosen-mode conditions as trigger options.

Exact edits: each must apply once, or have been applied (idempotent). Fails when a doc still shows one of the changed
conditions as a `ConditionalEffect` or a chosen mode as an intervening "if".

Usage: step15-run17n-docs.py
"""
import pathlib
import sys

DOCS = pathlib.Path("agent-docs")

EDITS = {
    DOCS / "EFFECTS_INDEX.md": [
        ('''| `ConditionalEffect(new PermanentEnteredThisTurn(predicate, minCount), wrapped)` | see construction shape | Wraps any effect with "if at least N permanents matching predicate entered the battlefield under that player's control this turn" condition. Pass `CountScope.ANY_PLAYER` for unqualified entry wording. Checked at trigger time via `permanentsEnteredBattlefieldThisTurn`. Used by Tunnel Ignus with `CardTypePredicate(LAND), 2` |''',
         '''| `PermanentEnteredThisTurn(predicate, minCount[, CountScope])` | see construction shape | "If at least N permanents matching predicate entered the battlefield under [your / any player's / that player's] control this turn" (`CONTROLLER` the default, `ANY_PLAYER` for unqualified entry wording, `TARGET_PLAYER` for "that player" of the trigger event: the context's target, else the triggering permanent's controller). Read from `permanentsEnteredBattlefieldThisTurn`. As an intervening "if", `TriggerOptions.DEFAULT.withInterveningIf(...)`: Tunnel Ignus and Confounding Conundrum with `CardTypePredicate(LAND), 2, TARGET_PLAYER` |'''),
        ('''| `ConditionalEffect(new ImprintedCardNameMatchesEnteringPermanent(), wrapped)` | see construction shape | Wraps any Triggers.enters(Triggers.LAND, ObjectRelation.OPPONENT_CONTROLS) effect with "entering permanent name matches source's imprinted card name" condition. Checked at trigger time. Used by Invader Parasite |''',
         '''| `ImprintedCardNameMatchesEnteringPermanent()` | see construction shape | The entering permanent's name matches the source's imprinted card's: the event condition (`TriggerOptions.DEFAULT.withEventCondition(...)`) of `Triggers.enters(Triggers.LAND, ObjectRelation.OPPONENT_CONTROLS)`, "whenever a land an opponent controls with the same name as the exiled card enters". Used by Invader Parasite |'''),
    ],
    DOCS / "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("""Triggers.enters(Triggers.LAND, ObjectRelation.OPPONENT_CONTROLS) ConditionalEffect(new ImprintedCardNameMatchesEnteringPermanent(), DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER)) —""",
         """Triggers.enters(Triggers.LAND, ObjectRelation.OPPONENT_CONTROLS) + `TriggerOptions.DEFAULT.withEventCondition(new ImprintedCardNameMatchesEnteringPermanent())`, DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER) —"""),
    ],
    DOCS / "ACTIVATED_ABILITY_GUIDE.md": [
        ("""| Opponent's land enters. Wrap with `ConditionalEffect(new PermanentEnteredThisTurn(predicate, minCount), wrapped)` for "second+ land" |""",
         """| Opponent's land enters. "If that player had another land enter the battlefield under their control this turn" is `TriggerOptions.DEFAULT.withInterveningIf(new PermanentEnteredThisTurn(predicate, 2, CountScope.TARGET_PLAYER))` (Tunnel Ignus) |"""),
    ],
    DOCS / "CARD_IMPLEMENTATION_PLAYBOOK.md": [
        ("""Defaults to permanents entering under the controller's control; pass `CountScope.ANY_PLAYER` for an unqualified "a permanent entered" condition |""",
         """Defaults to permanents entering under the controller's control; pass `CountScope.ANY_PLAYER` for an unqualified "a permanent entered" condition, `CountScope.TARGET_PLAYER` for "that player" of a trigger event (as an intervening "if": Tunnel Ignus) |"""),
    ],
    DOCS / "ORACLE_TEXT_EFFECT_MAP.md": [
        ('''`TriggerOptions.DEFAULT.withInterveningIf(new SourceHasChosenMode("Sultai")).withLimit(TriggerLimit.ONCE_EACH_TURN)`''',
         '''`TriggerOptions.DEFAULT.withEventCondition(new SourceHasChosenMode("Sultai")).withLimit(TriggerLimit.ONCE_EACH_TURN)` (the mode's ability exists only under it)'''),
    ],
}

FORBIDDEN = ["Wrap with `ConditionalEffect(new PermanentEnteredThisTurn", "| `ConditionalEffect(new PermanentEnteredThisTurn(predicate, minCount), wrapped)` | see construction", "ConditionalEffect(new ImprintedCardNameMatches",
             "withInterveningIf(new SourceHasChosenMode", "ConditionalEffect(new TargetPlayerControlsMoreLandsThanController"]


def main():
    failed = False
    for path, edits in EDITS.items():
        text = path.read_text()
        for old, new in edits:
            if text.count(old) == 1:
                text = text.replace(old, new)
            elif new not in text:
                print(f"{path}: edit not found: {old[:90]}")
                failed = True
        path.write_text(text)
    for path in list(DOCS.glob("*.md")) + list(pathlib.Path(".claude/skills").glob("*/SKILL.md")):
        for number, line in enumerate(path.read_text().splitlines(), 1):
            for pattern in FORBIDDEN:
                if pattern in line:
                    print(f"{path}:{number}: still shows {pattern}")
                    failed = True
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
