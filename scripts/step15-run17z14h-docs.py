#!/usr/bin/env python3
"""Step 15, run 17z14h: `agent-docs` and the card skills say how a Saga chapter and an "attacks or blocks" ability are
written (row 17z14h).

- A chapter is one `addTrigger` of all its effects (the cards no longer build one over several calls).
- "Whenever this creature attacks or blocks" is `TriggerOptions.ONE_OR_MORE` like "whenever this creature blocks"
  (CR 509.3a); "blocks or becomes blocked by a creature" triggers for each creature (CR 509.3b).
- The Saga rules these files cite as CR 714.2b to 714.2e are CR 715.2b to 715.2e (checked with the rules MCP: 714 is
  substitute cards now).

Fails if an edit didn't apply; idempotent.
"""
import pathlib

REPO = pathlib.Path(__file__).resolve().parent.parent

# file -> [(old, new)]
EDITS = {
    "agent-docs/CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md": [
        ("every effect added with the same chapter joins one ability, in the order added, so a chapter may be built "
         "over several `addTrigger` calls, and \"I, II —\" is one call per chapter (CR 714.2c).",
         "a chapter is one chapter ability, so write it as one `addTrigger` of all its effects, in the order the "
         "Oracle text has them (a second call with the same chapter would join the first's ability, but no card "
         "writes one), and \"I, II —\" is one call per chapter (CR 714.2c)."),
    ],
    "agent-docs/ACTIVATED_ABILITY_GUIDE.md": [
        ("(CR 509.3a); bushido's \"blocks\" half is written that way.",
         "(CR 509.3a); bushido's \"blocks\" half is written that way, and so is \"whenever this creature attacks or "
         "blocks\": `addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), TriggerOptions.ONE_OR_MORE, ...)` "
         "(a creature attacks once each combat anyway). \"Blocks or becomes blocked by a creature\" is for each "
         "creature (Ashmouth Hound's ruling): `Triggers.blocks()` as written beside `Triggers.becomesBlocked()` with "
         "`TriggerOptions.EACH_BLOCKER`."),
    ],
}
# The files whose Saga citations are renumbered.
RENUMBERED = [
    "agent-docs/CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md", "agent-docs/ARCHITECTURE.md",
    "agent-docs/ACTIVATED_ABILITY_GUIDE.md", ".claude/skills/implement-card/SKILL.md",
    ".claude/skills/review-card/SKILL.md",
]


def main():
    for name, edits in EDITS.items():
        path = REPO / name
        text = path.read_text()
        for old, new in edits:
            renumbered_new = new.replace("CR 714.2", "CR 715.2")
            if new in text or renumbered_new in text:
                continue
            if text.count(old) != 1:
                raise SystemExit(f"{name}: edit didn't apply")
            text = text.replace(old, new)
        path.write_text(text)
    for name in RENUMBERED:
        path = REPO / name
        text = path.read_text()
        if "CR 714.2" in text:
            path.write_text(text.replace("CR 714.2", "CR 715.2"))
        elif "CR 715.2" not in text:
            raise SystemExit(f"{name}: no Saga citation")


if __name__ == "__main__":
    main()
