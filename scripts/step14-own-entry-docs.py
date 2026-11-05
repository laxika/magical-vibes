#!/usr/bin/env python3
"""EffectSlot refactor step 14, run 16q: the agent-docs and skill passages about `OnceOnlyTriggerEffect`, which the
run deleted (only the legacy view built it; every "this ability triggers only once" is
`TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE)`). Exact replacements; running it again changes nothing.

    python3 scripts/step14-own-entry-docs.py [--dry-run]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
ONCE = ('"This ability triggers only once" is `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE)` on any trigger '
        '(Ruby Collector, Sleuth Instructor, Jet Collector)')

EDITS = {
    "agent-docs/EFFECTS_INDEX.md": [
        ("| `OnceOnlyTriggerEffect` | `(CardEffect wrapped)` | \"This ability triggers only once\" wrapper that consumes "
         "the source permanent object's marker on its first successful trigger; a new permanent object after leaving "
         "and returning can trigger again. Used by Sleuth Instructor (`YMKM` 2). An \"at the beginning of [step or "
         "phase]\" ability uses `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE)` instead (Jet Collector) |\n", ""),
    ],
    "agent-docs/EFFECTS_QUICK_REFERENCE.md": [
        ("- `OnceOnlyTriggerEffect(CardEffect)` — \"This ability triggers only once\"; marks the source permanent object "
         "in `GameData.onceOnlyTriggersFired` and does not reset at turn boundaries. A new permanent object created "
         "when the card changes zones may trigger its own once-only ability again (Ruby Collector, Sleuth Instructor). "
         "An \"at the beginning of [step or phase]\" ability uses `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE)` "
         "instead (Jet Collector, Acrobatic Cheerleader).",
         "- " + ONCE + "; the limit is keyed by the ability on the source permanent object and doesn't reset at turn "
         "boundaries, so a new permanent object created when the card changes zones may trigger it again (CR 400.7)."),
    ],
    "agent-docs/CARD_PATTERN_INDEX.md": [
        ("`r/RubyCollector.java` + `ConditionalEffect(MinimumAttackers(3), OnceOnlyTriggerEffect("
         "ConjureCardNamedIntoHandEffect(\"LEA\", \"264\", false)))`",
         "`r/RubyCollector.java` + `addTrigger(Triggers.youAttack(), TriggerOptions.DEFAULT.withInterveningIf("
         "new MinimumAttackers(3)).withLimit(TriggerLimit.ONCE), ConjureCardNamedIntoHandEffect(\"LEA\", \"264\", "
         "false))`"),
    ],
    ".claude/skills/implement-card/SKILL.md": [
        ("`OncePerTurnTriggerEffect`, `OnceOnlyTriggerEffect` or an intervening-if `ConditionalEffect`",
         "`OncePerTurnTriggerEffect` or an intervening-if `ConditionalEffect`"),
        ("`TriggeringCardConditionalEffect`, `OncePerTurnTriggerEffect` or `OnceOnlyTriggerEffect` around",
         "`TriggeringCardConditionalEffect` or `OncePerTurnTriggerEffect` around"),
    ],
    ".claude/skills/review-card/SKILL.md": [
        ("/ `OncePerTurnTriggerEffect` / `OnceOnlyTriggerEffect` / intervening-if `ConditionalEffect`, and",
         "/ `OncePerTurnTriggerEffect` / intervening-if `ConditionalEffect`, and"),
        ("`TriggeringCardConditionalEffect` / `OncePerTurnTriggerEffect` / `OnceOnlyTriggerEffect` / intervening-if",
         "`TriggeringCardConditionalEffect` / `OncePerTurnTriggerEffect` / intervening-if"),
    ],
}


def main():
    dry = "--dry-run" in sys.argv
    for name, edits in EDITS.items():
        path = ROOT / name
        text = path.read_text()
        new = text
        for old, replacement in edits:
            if old in new:
                new = new.replace(old, replacement)
            elif replacement and replacement in new:
                continue
            elif not replacement:
                continue
            else:
                raise SystemExit(f"{name}: passage not found: {old[:80]}")
        if new != text:
            print(name)
            if not dry:
                path.write_text(new)


if __name__ == "__main__":
    main()
