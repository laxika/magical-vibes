#!/usr/bin/env python3
"""Step 15, run 17z13: agent-docs follow the run. `LoseGameIfNotCastFromHandEffect` is gone (Phage writes the
intervening "if" and `ControllerLosesGameEffect`); `ColorManaPairsSpentToCast` reads the permanent's record of the
payment as each enters ability resolves (the enters resolver no longer freezes it into the effects). Idempotent; fails
if a doc still names the removed effect."""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"


def edit(name, pairs):
    path = DOCS / name
    src = path.read_text()
    changed = False
    for old, new in pairs:
        if new in src:
            continue
        if src.count(old) != 1:
            sys.exit(f"{name}: expected one match of\n{old}")
        src = src.replace(old, new)
        changed = True
    path.write_text(src)
    print(("updated " if changed else "already updated ") + name)


edit("EFFECTS_INDEX.md", [
    ("| `LoseGameIfNotCastFromHandEffect` | `()` | lose the game if not cast from hand (ETB check) |\n", ""),
    ("EtbEffectResolver snapshots ColorManaPairsSpentToCast into Fixed amounts for draw, discard, and self-boost "
     "effects, including sequence and conditional wrappers. Independent entering abilities retain the same casting "
     "payment even after another ability finishes resolving; other dynamic amounts remain live.",
     "ColorManaPairsSpentToCast reads the mana of each color the permanent recorded as its spell resolved "
     "(`Permanent.manaSpentByColorToCast`, from `StackEntry.manaSpentByColorToCast`), so each of its independent "
     "enters abilities reads the same payment as it resolves, after another has resolved; its effects go on the stack "
     "as written. Before the permanent exists (an \"enters with\" replacement) it reads the spell's payment."),
])
edit("ORACLE_TEXT_EFFECT_MAP.md", [
    ("Bladecoil Serpent (BRO). The cast path snapshots exact colored mana totals; each complete pair contributes one, "
     "and odd amounts round down. |",
     "Bladecoil Serpent (BRO). The permanent records the exact colored mana totals as its spell resolves and each "
     "enters ability reads them as it resolves; each complete pair contributes one, and odd amounts round down. |"),
    ("| \"Whenever a nontoken creature you control enters, if you didn't cast it from your hand,",
     "| \"When this enters, if you didn't cast it from your hand, you lose the game.\" | "
     "`TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new CastFromZone(Zone.HAND)))` + "
     "`new ControllerLosesGameEffect()` | Triggers.enters(ObjectRelation.THIS) | Phage the Untouchable (LGN). "
     "Putting it onto the battlefield from your hand without casting it, or casting it from anywhere else, loses; "
     "the entry fact is judged as the ability triggers. |\n"
     "| \"Whenever a nontoken creature you control enters, if you didn't cast it from your hand,"),
])

for doc in DOCS.glob("*.md"):
    if "LoseGameIfNotCastFromHandEffect" in doc.read_text():
        sys.exit(f"{doc.name} still names LoseGameIfNotCastFromHandEffect")
