#!/usr/bin/env python3
"""Step 15, run 17z13e8b: agent-docs follow the draw readers.

- `TRIGGER_SLOT_TARGETING.md`: the draw readers read each ability whole (one object on the stack, CR 603.3) and put an
  untargeted one, a "you may" included, on the stack as written with the drawing player as its non-targeting "that
  player" and triggering player, the drawn card and the source as it was (`DrawService.recordDrawEvent`).
- `CARD_PATTERNS_PERMANENTS_ARTIFACTS.md`: Diviner's Wand grants its draw ability to the equipped creature
  (`GrantTriggeredAbilityEffect`), not a draw ability of the Equipment the reader skipped while unattached.

Idempotent; fails if a doc still names a removed form. Usage: step15-run17z13e8b-docs.py
"""
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = [
    ("TRIGGER_SLOT_TARGETING.md",
     "Non–any-target draw triggers (Psychosis Crawler) still push a non-targeting entry straight to the stack. |",
     "Every draw reader reads an ability whole: its effects are one object on the stack (CR 603.3), a target chosen "
     "for the ability as it goes there; an untargeted one (Psychosis Crawler, a \"you may\" such as Hoofprints of the "
     "Stag's or Consecrated Sphinx's, asked as it resolves, CR 603.5) goes on the stack as written with the drawing "
     "player as its non-targeting \"that player\" and triggering player, the drawn card as its triggering card and "
     "the source as it was (`DrawService.recordDrawEvent`). |"),
    ("CARD_PATTERNS_PERMANENTS_ARTIFACTS.md",
     "| Draw trigger boost/flying + granted draw ability equip | `d/DivinersWand.java` | Triggers.youDrawCard() "
     "BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(1, 1, FLYING) + STATIC",
     "| Granted draw trigger boost/flying + granted draw ability equip | `d/DivinersWand.java` | STATIC "
     "GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.youDrawCard(), SequenceEffect.of(BoostSelfEffect(1, "
     "1), GrantKeywordEffect(FLYING, SELF))), EQUIPPED_CREATURE) + STATIC"),
    ("CARD_PATTERNS_PERMANENTS_ARTIFACTS.md",
     "\"Whenever you draw a card, equipped creature gets +1/+1 and gains flying until end of turn\" modeled as a draw "
     "trigger on the Equipment that boosts the attached creature (fizzles when unattached) |",
     "\"Equipped creature has 'Whenever you draw a card, this creature gets +1/+1 and gains flying until end of "
     "turn'\": the creature's own ability while equipped, so an unattached Wand has none |"),
]
GONE = [
    ("CARD_PATTERNS_PERMANENTS_ARTIFACTS.md", "modeled as a draw trigger on the Equipment"),
    ("TRIGGER_SLOT_TARGETING.md", "still push a non-targeting entry straight to the stack"),
]


def main():
    for name, old, new in EDITS:
        path = DOCS / name
        text = path.read_text()
        if new in text:
            continue
        if old not in text:
            raise SystemExit(f"{name}: anchor not found: {old[:60]}")
        path.write_text(text.replace(old, new, 1))
        print(f"edited agent-docs/{name}")
    for name, form in GONE:
        if form in (DOCS / name).read_text():
            raise SystemExit(f"{name} still describes a removed form: {form}")


if __name__ == "__main__":
    main()
