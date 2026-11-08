#!/usr/bin/env python3
"""Step 15, run 17w5: the agent-docs rows of Thousand-Year Storm, Trial and Error and The Lord of Pain.

Removes the rows of the three deleted records (`CopySpellForEachPriorInstantOrSorceryEffect`,
`CopyThisSpellWhenCounteredOrFizzlesEffect`, `FirstSpellCastTriggerEffect`), rewrites the three cards' Oracle-text rows
to the filtered cast trigger / the countered spell's own trigger with existing effects reading the stack entry, adds
`CopyTriggeringSpellEffect.withCopies(DynamicAmount)` and the new `SpellsCastBeforeTriggeringSpellThisTurn` amount,
replaces `PlayerOtherThanPredicate` with `PlayerOtherThanTriggeringPlayerPredicate` in `PREDICATES_REFERENCE.md`, and
adds a sentence to `ARCHITECTURE.md`'s triggering-spell paragraph. Idempotent. Fails if a doc or skill still names a
removed form.

Usage: step15-run17w5-docs.py
"""
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"
SKILLS = ROOT / ".claude/skills"

REMOVED = ["CopySpellForEachPriorInstantOrSorceryEffect", "CopyThisSpellWhenCounteredOrFizzlesEffect",
           "FirstSpellCastTriggerEffect", "PlayerOtherThanPredicate`"]

NEW_AMOUNT = ("`SpellsCastBeforeTriggeringSpellThisTurn(CardPredicate filter)` — the matching spells the triggering "
              "spell's caster cast this turn before it, counted as the ability resolves up to the triggering spell's "
              "place among its caster's spells this turn (kept on the stack entry as the ability triggers, so a spell "
              "cast in response doesn't count; a countered spell was still cast and counts). \"Copy it for each other "
              "instant and sorcery spell you've cast before it this turn\" (Thousand-Year Storm): "
              "`new CopyTriggeringSpellEffect().withCopies(new SpellsCastBeforeTriggeringSpellThisTurn(instantOrSorcery))`")

LORD = ("| \"Whenever a player casts their first spell each turn, choose another target player. The Lord of Pain deals "
        "damage equal to that spell's mana value to the chosen player\" | `target(new PlayerPredicateTargetFilter(new "
        "PlayerOtherThanTriggeringPlayerPredicate(), \"Target must be another player\")).addTrigger(…, new "
        "DealDamageToPlayersEffect(new TargetSpellManaValue(), DamageRecipient.TARGET_PLAYER))` | "
        "`Triggers.playerCastsSpell().nthThisTurn(1)` | The Lord of Pain. `nthThisTurn(1)` counts each casting player's "
        "spells; the target choice carries the caster as its triggering player, which "
        "`PlayerOtherThanTriggeringPlayerPredicate` excludes (and the entry's as it resolves); the spell's mana value is "
        "read from the entry's triggering spell as it resolves. |")
STORM = ("| \"Whenever you cast an instant or sorcery spell, copy it for each other instant and sorcery spell you've cast "
         "before it this turn. You may choose new targets for the copies.\" | `new CopyTriggeringSpellEffect().withCopies("
         "new SpellsCastBeforeTriggeringSpellThisTurn(instantOrSorcery))` | `Triggers.youCastSpell().withFilter("
         "instantOrSorcery)` | Thousand-Year Storm. The copies are made from the stack entry's triggering spell as it "
         "resolves; the count reads the caster's spells cast before it (its place among them is kept as the ability "
         "triggers), so spells cast in response and other players' spells don't add copies |")
TRIAL = ("| \"Whenever Trial and Error is countered or fizzles, you may copy it and choose new targets for the copy. Trial "
         "and Error deals 3 damage to any target.\" | `new MayEffect(new CopyTriggeringSpellEffect(), \"Copy Trial and "
         "Error?\")` + `SPELL DealDamageToAnyTargetEffect(3)` | Triggers.thisSpellCounteredOrFizzles() + SPELL | Trial "
         "and Error (MB1 #68). The countered spell, as it last existed on the stack, is the entry's triggering spell; a "
         "copy of it that fizzles triggers and is copied too |")

ARCH_ANCHOR = ("reads the spell, the caster and the source permanent from the entry as it resolves.")
ARCH_ADD = (" \"Copy it for each other instant and sorcery spell you've cast before it this turn\" (Thousand-Year Storm) is "
            "`CopyTriggeringSpellEffect().withCopies(new SpellsCastBeforeTriggeringSpellThisTurn(filter))`, counted as it "
            "resolves up to the triggering spell's place among its caster's spells this turn, which the cast collector "
            "keeps on the entry (`StackEntry.triggeringSpellCastIndex`). A targeted cast trigger's choice carries the "
            "caster as its triggering player (`SpellTargetTriggerAnyTarget.withTriggeringPlayer`), so \"another target "
            "player\" (`PlayerOtherThanTriggeringPlayerPredicate`, The Lord of Pain) is judged against the caster as the "
            "target is chosen and against the entry's triggering player as it resolves. A spell's own \"whenever this "
            "spell is countered or fizzles\" ability (Trial and Error) has the spell as it last existed on the stack as "
            "its triggering spell.")

# file -> list of (kind, anchor, text):
#   "line": the line starting with anchor is replaced whole by text
#   "drop": the line starting with anchor is removed
#   "sub": anchor (a substring) is replaced by text, unless text is already present
#   "before": a line text is inserted before the line starting with anchor, unless text is already present
EDITS = {
    "EFFECTS_INDEX.md": [
        ("drop", "| `FirstSpellCastTriggerEffect` |", None),
        ("drop", "| `CopySpellForEachPriorInstantOrSorceryEffect` |", None),
        ("drop", "| `CopyThisSpellWhenCounteredOrFizzlesEffect` |", None),
        ("sub", "`withCopies(n)` (\"copy it twice\").",
         "`withCopies(n)` (\"copy it twice\"), `withCopies(DynamicAmount)` (counted as the ability resolves, no copy at "
         "zero: Thousand-Year Storm's `SpellsCastBeforeTriggeringSpellThisTurn`). On `Triggers.thisSpellCounteredOrFizzles()` "
         "it copies the countered spell, a copy too (Trial and Error: `new MayEffect(new CopyTriggeringSpellEffect(), "
         "\"Copy Trial and Error?\")`)."),
        ("before", "- `SpellsCastFromOutsideHandThisTurn(CountScope scope)`", "- " + NEW_AMOUNT),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("drop", "- `FirstSpellCastTriggerEffect(", None),
        ("drop", "- `CopySpellForEachPriorInstantOrSorceryEffect()`", None),
        ("drop", "- `CopyThisSpellWhenCounteredOrFizzlesEffect()`", None),
        ("sub", "`asAdditionally(subtypes, p, t)`, `withCopies(n)`;",
         "`asAdditionally(subtypes, p, t)`, `withCopies(n)`, `withCopies(DynamicAmount)` (counted as it resolves: "
         "Thousand-Year Storm's `new SpellsCastBeforeTriggeringSpellThisTurn(instantOrSorcery)`);"),
        ("sub", "In a `Triggers.youCastThisSpell()` ability it copies this spell (\"when you cast this spell, copy it\", "
                "below). In a `Triggers.youCastThisSpell()` ability it copies this spell (\"when you cast this spell, "
                "copy it\", below).",
         "In a `Triggers.youCastThisSpell()` ability it copies this spell (\"when you cast this spell, copy it\", "
         "below). On `Triggers.thisSpellCounteredOrFizzles()` it copies the countered spell, a copy too (Trial and "
         "Error: `new MayEffect(new CopyTriggeringSpellEffect(), \"Copy Trial and Error?\")`)."),
        ("before", "- `TotalManaValueOfOtherSpellsCastThisTurn()`", "- " + NEW_AMOUNT),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("line", "| \"Whenever a player casts their first spell each turn, choose another target player. The Lord of Pain",
         LORD),
        ("line", "| \"Whenever you cast an instant or sorcery spell, copy it for each other instant and sorcery spell "
                 "you've cast before it this turn.", STORM),
        ("line", "| \"Whenever Trial and Error is countered or fizzles,", TRIAL),
    ],
    "PREDICATES_REFERENCE.md": [
        ("line", "| `PlayerOtherThanPredicate` |",
         "| `PlayerOtherThanTriggeringPlayerPredicate` | `()` | any player other than the one the triggered ability's "
         "event was about: the target choice's triggering player as the target is chosen, the stack entry's "
         "(`StackEntry.triggeringPlayerId`) as it resolves; a cast trigger's is the caster. \"Choose another target "
         "player\" (The Lord of Pain); matches nobody without one | "),
    ],
    "ARCHITECTURE.md": [
        ("sub", ARCH_ANCHOR, ARCH_ANCHOR + ARCH_ADD),
    ],
}


def apply(name, edits):
    path = DOCS / name
    text = path.read_text()
    original = text
    for kind, anchor, new in edits:
        lines = text.split("\n")
        if kind == "sub":
            if new in text:
                continue
            if text.count(anchor) != 1:
                raise SystemExit(f"{name}: anchor not found once: {anchor[:80]}")
            text = text.replace(anchor, new)
            continue
        if kind == "before":
            if new in lines:
                continue
            index = [i for i, l in enumerate(lines) if l.startswith(anchor)]
            if len(index) != 1:
                raise SystemExit(f"{name}: anchor line not found once: {anchor[:80]}")
            lines.insert(index[0], new)
        elif kind == "drop":
            lines = [l for l in lines if not l.startswith(anchor)]
        elif kind == "line":
            if new.rstrip() in [l.rstrip() for l in lines]:
                continue
            index = [i for i, l in enumerate(lines) if l.startswith(anchor)]
            if len(index) != 1:
                raise SystemExit(f"{name}: anchor line not found once: {anchor[:80]}")
            lines[index[0]] = new.rstrip()
        text = "\n".join(lines)
    if text != original:
        path.write_text(text)
        print(f"updated {name}")


def check():
    stale = []
    for path in list(DOCS.glob("*.md")) + list(SKILLS.rglob("*.md")):
        text = path.read_text()
        for removed in REMOVED:
            if removed in text:
                stale.append(f"{path.relative_to(ROOT)}: {removed}")
    if stale:
        raise SystemExit("still naming a removed form:\n" + "\n".join(stale))


def main():
    for name, edits in EDITS.items():
        apply(name, edits)
    check()


if __name__ == "__main__":
    main()
