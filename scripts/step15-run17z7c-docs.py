#!/usr/bin/env python3
"""Step 15, run 17z7c: the agent-docs rows of Lucy MacLean, Positively Armed.

The card writes its own "you may" as a `MayEffect` around `CreateTokenCopyOfEnteringTokenForTargetPlayerEffect`; the
token-enter collectors queue its target choice with the entering token as the entry's triggering permanent and carry the
ability's "do this only once each turn" to the entry.

Idempotent (whole-fragment edits).

Usage: step15-run17z7c-docs.py
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| `CreateTokenCopyOfEnteringTokenForTargetPlayerEffect()` with `TriggerOptions.DEFAULT.withLimit("
         "TriggerLimit.DO_ONLY_ONCE_EACH_TURN)` |",
         "| `MayEffect(CreateTokenCopyOfEnteringTokenForTargetPlayerEffect(), prompt)` with `TriggerOptions.DEFAULT"
         ".withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN)` |"),
        ("Lucy MacLean, Positively Armed (SLD 2447). The enter-trigger target collector excludes the entering token's "
         "controller;",
         "Lucy MacLean, Positively Armed (SLD 2447). The card writes its \"you may\", asked as the ability resolves "
         "(CR 603.5) after the target is chosen as it goes on the stack (CR 603.3d); the entering token is the stack "
         "entry's triggering permanent, and the token-enter target choice carries the \"do this only once each turn\" "
         "(CR 603.2h) to the entry. The enter-trigger target collector excludes the entering token's controller;"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `CreateTokenCopyOfEnteringTokenForTargetPlayerEffect()` — targeted enter-trigger effect: a player other "
         "than the entering token's controller may create",
         "- `CreateTokenCopyOfEnteringTokenForTargetPlayerEffect()` — targeted enter-trigger effect, inside the card's "
         "`MayEffect`: a player other than the entering token's controller (the stack entry's triggering permanent) "
         "creates"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `CreateTokenCopyOfEnteringTokenForTargetPlayerEffect` | `()` | targeted token-entry trigger: a player other "
         "than the entering token's controller may create",
         "| `CreateTokenCopyOfEnteringTokenForTargetPlayerEffect` | `()` | targeted token-entry trigger, inside the "
         "card's `MayEffect`: a player other than the entering token's controller (the entry's triggering permanent) "
         "creates"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| `CreateTokenCopyOfEnteringTokenForTargetPlayerEffect()` on both `Triggers.enters(Triggers.TOKEN, "
         "ObjectRelation.YOU_CONTROL)`",
         "| `MayEffect(CreateTokenCopyOfEnteringTokenForTargetPlayerEffect(), prompt)` on both `Triggers.enters("
         "Triggers.TOKEN, ObjectRelation.YOU_CONTROL)`"),
    ],
}


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


if __name__ == "__main__":
    sys.exit(main())
