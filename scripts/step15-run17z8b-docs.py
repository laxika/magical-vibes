#!/usr/bin/env python3
"""Step 15, run 17z8b: the agent-docs rows of Riku of Many Paths' "whenever you cast a modal spell, choose up to X".

`ModalSpellCastTriggerEffect` is gone: the card writes `Triggers.youCastSpell().withStackEntryFilter(new
StackEntryIsModalSpellPredicate())` and `ChooseOneAtTriggerTimeEffect(modal, new TriggeringSpellModeCount())`, whose
maximum the cast collector evaluates from the triggering spell as the ability goes on the stack (CR 603.3c). The
predicate and the amount are new.

Idempotent (whole-fragment edits); fails if a doc or skill still names the removed record outside a "Gone" line.

Usage: step15-run17z8b-docs.py
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

REMOVED = ["ModalSpellCastTriggerEffect"]

FANTASTIC_FOUR_TAIL = ("The stack-entry filter reads the cast-time mana value (with X), printed power or printed toughness. |\n")
RIKU_ROW = ("| \"Whenever you cast a modal spell, choose up to X, where X is the number of times you chose a mode for that "
            "spell —\" | `new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(options, true, 0, options.size()), new "
            "TriggeringSpellModeCount())` | `Triggers.youCastSpell().withStackEntryFilter(new "
            "StackEntryIsModalSpellPredicate())` | Riku of Many Paths (OTJ 227). The cast collector evaluates the "
            "maximum from the triggering spell as the ability goes on the stack, where its modes are chosen (CR 603.3c); "
            "X counts a mode chosen more than once each time, and with X = 0 no mode can be chosen, so the ability is "
            "removed (CR 700.2b). Its own modes can't repeat. |\n")

QUICK_AMOUNT_ANCHOR = "- `new TriggeringPermanentManaValue()` — the mana value of the stack entry's triggering permanent,"
QUICK_AMOUNT = ("- `new TriggeringSpellModeCount()` — \"the number of times you chose a mode for that spell\" (Riku of Many "
                "Paths): the modes chosen for a cast trigger's triggering spell, a repeated mode counted each time (CR "
                "700.2d), read from the spell on the stack, else the entry's snapshot of it; 0 for a spell that isn't "
                "modal. As a cast trigger's `ChooseOneAtTriggerTimeEffect.maximumChoices`, it is read as the ability goes "
                "on the stack.\n")

PREDICATE_ANCHOR = "| `StackEntryManaValuePowerOrToughnessEqualsPredicate` | `(int number)` |"
PREDICATE_ROW = ("| `StackEntryIsModalSpellPredicate` | `()` | modal spells (\"whenever you cast a modal spell\", Riku of Many "
                 "Paths): a spell whose modes were chosen as it was cast (CR 700.2a), so its entry records its mode "
                 "count; a creature spell with a modal enters ability isn't one |\n")

INDEX_OLD = ("`ChooseOneEffectHandler` (`ChoiceContext.ChooseModeChoice`); use `ChooseOneAtTriggerTimeEffect` for those.\n")
INDEX_NEW = ("`ChooseOneEffectHandler` (`ChoiceContext.ChooseModeChoice`); use `ChooseOneAtTriggerTimeEffect` for those.\n"
             "A cast trigger's lone `ChooseOneAtTriggerTimeEffect(modal, maximumChoices)` (\"choose up to X\", Riku of "
             "Many Paths' `TriggeringSpellModeCount`) has its maximum evaluated by the cast collector against the "
             "triggering spell as the ability goes on the stack; a maximum of 0 offers no mode, and the ability is "
             "removed (CR 700.2b).\n")


def insert_after_line(text, anchor, line):
    if line in text:
        return text
    if text.count(anchor) != 1:
        raise SystemExit(f"expected exactly one occurrence of: {anchor[:100]}")
    start = text.index(anchor)
    end = text.index("\n", start) + 1
    return text[:end] + line + text[end:]


def edit(name, transform):
    path = DOCS / name
    path.write_text(transform(path.read_text()))
    print(f"{name}: up to date")


def main():
    edit("ORACLE_TEXT_EFFECT_MAP.md", lambda t: insert_after_line(t, FANTASTIC_FOUR_TAIL, RIKU_ROW))
    edit("EFFECTS_QUICK_REFERENCE.md", lambda t: insert_after_line(t, QUICK_AMOUNT_ANCHOR, QUICK_AMOUNT))
    edit("PREDICATES_REFERENCE.md", lambda t: insert_after_line(t, PREDICATE_ANCHOR, PREDICATE_ROW))
    edit("EFFECTS_INDEX.md", lambda t: t if INDEX_NEW in t else t.replace(INDEX_OLD, INDEX_NEW, 1)
         if t.count(INDEX_OLD) == 1 else (_ for _ in ()).throw(SystemExit("EFFECTS_INDEX.md: anchor missing")))
    skills = ROOT / ".claude/skills"
    paths = list(DOCS.glob("*.md")) + (list(skills.rglob("*.md")) if skills.is_dir() else [])
    for path in paths:
        for line in path.read_text().splitlines():
            if any(form in line for form in REMOVED) and "**Gone**" not in line:
                raise SystemExit(f"{path}: still names a removed form: {line[:120]}")


if __name__ == "__main__":
    sys.exit(main())
