#!/usr/bin/env python3
"""Step 15, run 17z13e8c: the authoring rule the step readers now rely on, in agent-docs and the card skills.

Every step reader reads each triggered ability whole, as one stack object (CR 603.3), and no longer merges a card's
separate `addTrigger` calls on one step trigger; one Oracle ability is one `addTrigger` with every effect, an ability
of several target groups binding each effect to its group with `bindEffect`. Inserts the rule once in each file (by a
marker), and fails if a doc still describes the removed reader merges or the beginning-of-combat Equipment skip.

Usage: step15-run17z13e8c-docs.py [REPOSITORY_ROOT]. Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent)

RULE = ("**One Oracle ability is one `addTrigger`.** A triggered ability is one object on the stack (CR 603.3) with "
        "every effect it has, in order: \"at the beginning of combat on your turn, target creature you control gets "
        "+1/+0 and gains haste until end of turn\" is `target(creatureYouControl).addTrigger("
        "Triggers.beginningOfCombat(ActivePlayer.YOU), new BoostTargetCreatureEffect(1, 0), new GrantKeywordEffect("
        "Keyword.HASTE, GrantScope.TARGET))`, never two `addTrigger` calls, which are two abilities that go on the "
        "stack, and are targeted, separately. An ability of several target groups (\"up to one target creature "
        "attacks or blocks this combat if able and up to one target creature can't attack or block this combat\", "
        "Boros Battleshaper) is one `addTrigger` of its effects, each bound to its group with "
        "`target(filter, 0, 1).bindEffect(effect)`. Write two `addTrigger` calls only where the card prints two "
        "abilities (Lunar Convocation's two end-step abilities, Bloodhall Ooze's two upkeep ones).")
MARKER = "**One Oracle ability is one `addTrigger`.**"

INSERTS = [
    # (file, the line the rule goes before)
    (".claude/skills/implement-card/SKILL.md", "**A condition of the trigger event is an event condition.**"),
    ("agent-docs/TRIGGER_SLOT_TARGETING.md", "## Trigger slot → pipeline mapping"),
]

PITFALL_OLD = """  declined, separately — beginning-of-combat triggers route there via `hasMultipleTargetGroups`
  (Boros Battleshaper)."""
PITFALL_NEW = """  declined, separately — the step readers route one ability there with its effects when the card has
  several groups or one of more than one target (Boros Battleshaper's one ability of two groups)."""

REVIEW_ANCHOR = "4. **Constructor hygiene**"
REVIEW_ITEM = ("3a. **One Oracle ability, one `addTrigger`** — an ability written as several `addTrigger` calls on one "
               "trigger is several stack objects (CR 603.3): merge them into one call of every effect, each bound to "
               "its target group (`bindEffect` for an ability of several groups).\n")

STALE = [
    "the beginning-of-combat reader merges",
    "only fire if the equipment is attached",
]


def main():
    for rel, anchor in INSERTS:
        path = ROOT / rel
        text = path.read_text()
        if MARKER not in text:
            if anchor not in text:
                raise SystemExit(f"{rel}: anchor not found")
            text = text.replace(anchor, RULE + "\n\n" + anchor, 1)
            path.write_text(text)
            print(f"updated {rel}")
    path = ROOT / "agent-docs/TRIGGER_SLOT_TARGETING.md"
    text = path.read_text()
    if PITFALL_OLD in text:
        path.write_text(text.replace(PITFALL_OLD, PITFALL_NEW))
        print("updated agent-docs/TRIGGER_SLOT_TARGETING.md pitfall")
    elif PITFALL_NEW not in text:
        raise SystemExit("TRIGGER_SLOT_TARGETING.md: pitfall not found")
    path = ROOT / ".claude/skills/review-card/SKILL.md"
    text = path.read_text()
    if REVIEW_ITEM not in text:
        if REVIEW_ANCHOR not in text:
            raise SystemExit("review-card/SKILL.md: anchor not found")
        path.write_text(text.replace(REVIEW_ANCHOR, REVIEW_ITEM + REVIEW_ANCHOR, 1))
        print("updated .claude/skills/review-card/SKILL.md")
    for doc in [*(ROOT / "agent-docs").glob("*.md"), *(ROOT / ".claude/skills").rglob("*.md")]:
        lowered = doc.read_text().lower()
        for phrase in STALE:
            if phrase in lowered:
                raise SystemExit(f"{doc.relative_to(ROOT)} still says: {phrase}")


if __name__ == "__main__":
    main()
