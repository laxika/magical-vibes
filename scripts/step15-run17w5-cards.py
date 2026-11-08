#!/usr/bin/env python3
"""Step 15, run 17w5: Thousand-Year Storm, Trial and Error and The Lord of Pain read the stack entry.

Rewrites the three card classes whose ability was a record a collector judged by class and rebuilt with the triggering
spell bound in:

- Thousand-Year Storm: a filtered cast trigger (an instant or sorcery) whose `CopyTriggeringSpellEffect` counts its
  copies as it resolves with the new `SpellsCastBeforeTriggeringSpellThisTurn` amount ("for each other instant and
  sorcery spell you've cast before it this turn"), replacing `CopySpellForEachPriorInstantOrSorceryEffect`.
- Trial and Error: "you may copy it and choose new targets for the copy" is `MayEffect(CopyTriggeringSpellEffect())`,
  the countered spell the entry's triggering spell, replacing `CopyThisSpellWhenCounteredOrFizzlesEffect`.
- The Lord of Pain: "their first spell each turn" is `nthThisTurn(1)`, "another target player" the card's target filter
  `PlayerOtherThanTriggeringPlayerPredicate`, "that spell's mana value" `TargetSpellManaValue`, replacing
  `FirstSpellCastTriggerEffect`.

Idempotent: a file already converted is left alone. Per letter module.

Usage: step15-run17w5-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

# card -> (letter, [(old, new)], imports to add, imports to remove)
CARDS = {
    "ThousandYearStorm": ("t", [
        ("""        addTrigger(Triggers.youCastSpell(), new CopySpellForEachPriorInstantOrSorceryEffect());""",
         """        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery),
                new CopyTriggeringSpellEffect()
                        .withCopies(new SpellsCastBeforeTriggeringSpellThisTurn(instantOrSorcery)));"""),
    ], ["model.CardType", "model.amount.SpellsCastBeforeTriggeringSpellThisTurn",
        "model.effect.CopyTriggeringSpellEffect", "model.filter.CardAnyOfPredicate",
        "model.filter.CardTypePredicate", "java:java.util.List"],
        ["model.effect.CopySpellForEachPriorInstantOrSorceryEffect"]),
    "TrialAndError": ("t", [
        ("""        addTrigger(Triggers.thisSpellCounteredOrFizzles(),
                new CopyThisSpellWhenCounteredOrFizzlesEffect());""",
         """        addTrigger(Triggers.thisSpellCounteredOrFizzles(),
                new MayEffect(new CopyTriggeringSpellEffect(), "Copy Trial and Error?"));"""),
    ], ["model.effect.CopyTriggeringSpellEffect", "model.effect.MayEffect"],
        ["model.effect.CopyThisSpellWhenCounteredOrFizzlesEffect"]),
    "TheLordOfPain": ("t", [
        ("""        addTrigger(Triggers.playerCastsSpell(),
                new FirstSpellCastTriggerEffect(List.of(
                        new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TARGET_PLAYER))));""",
         """        target(new PlayerPredicateTargetFilter(new PlayerOtherThanTriggeringPlayerPredicate(),
                "Target must be another player"))
                .addTrigger(Triggers.playerCastsSpell().nthThisTurn(1),
                        new DealDamageToPlayersEffect(new TargetSpellManaValue(), DamageRecipient.TARGET_PLAYER));"""),
    ], ["model.amount.TargetSpellManaValue", "model.filter.PlayerOtherThanTriggeringPlayerPredicate",
        "model.filter.PlayerPredicateTargetFilter"],
        ["model.amount.EventValue", "model.effect.FirstSpellCastTriggerEffect", "java:java.util.List"]),
}


def imp(cls):
    return f"import {PKG}.{cls};\n"


def import_line(name):
    return f"import {name[5:]};\n" if name.startswith("java:") else imp(name)


def convert(name, letter, edits, add, remove):
    path = ROOT / f"magical-vibes-card/{letter}/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"
    text = path.read_text()
    original = text
    for old, new in edits:
        if new in text:
            continue
        if old not in text:
            raise SystemExit(f"{name}: expected text not found:\n{old}")
        text = text.replace(old, new)
    for cls in remove:
        text = text.replace(import_line(cls), "")
    lines = text.split("\n")
    for cls in add:
        line = import_line(cls).rstrip("\n")
        if line in lines:
            continue
        imports = [i for i, l in enumerate(lines) if l.startswith("import ")]
        same_kind = [i for i in imports if lines[i].startswith("import java.") == line.startswith("import java.")]
        if same_kind:
            position = next((i for i in same_kind if lines[i] > line), same_kind[-1] + 1)
        else:
            position = imports[-1] + 1
            lines.insert(position, "")
            position += 1
        lines.insert(position, line)
    text = "\n".join(lines)
    # A java.util import removed last leaves the blank line that separated it.
    text = text.replace("\n\n\n@CardRegistration", "\n\n@CardRegistration")
    if text != original:
        path.write_text(text)
        print(f"converted {name}")


def main():
    letters = set("abcdefghijklmnopqrstuvwxyz") if sys.argv[1:] == ["all"] else set(sys.argv[1:])
    if not letters:
        raise SystemExit(__doc__)
    for name, (letter, edits, add, remove) in CARDS.items():
        if letter in letters:
            convert(name, letter, edits, add, remove)


if __name__ == "__main__":
    main()
