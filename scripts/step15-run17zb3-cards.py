#!/usr/bin/env python3
"""Step 15, run 17zb3: the cast collector's class-keyed handlers for Harness the Storm, Toofer, Keeper of the Full Grip,
Spellweaver Helix and Bucket List are written as abilities whose effects read the stack entry's triggering spell.

`SpellCastTriggerCollectorService` read three markers and one effect by class and built the effect that went on the
stack from the event. Each card now writes the ability:

- Harness the Storm: "whenever you cast an instant or sorcery spell from your hand" is
  `Triggers.youCastSpell().withFilter(instant or sorcery).castFrom(Zone.HAND)`; "you may cast target card with the
  same name as that spell from your graveyard" is `CastTargetInstantOrSorceryFromGraveyardEffect(CONTROLLERS_GRAVEYARD,
  false, false, new CardSharesNameWithTriggeringSpellPredicate())`, its target chosen against the triggering spell;
- Toofer, Keeper of the Full Grip: "whenever a spell you control causes you to gain card advantage" is
  `Triggers.yourSpellCausesYouToGainCardAdvantage()`, an event of the spell's resolution, and "each opponent loses 2
  life" `LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT)`;
- Spellweaver Helix: "whenever a player casts a card" is `Triggers.playerCastsSpell()` without a cast copy (a copy
  isn't a card); "if it has the same name as one of the cards exiled with this artifact" the intervening "if"
  `TriggeringSpellMatches(new StackEntrySharesNameWithCardExiledWithSourcePredicate())`; "you may copy the other" the
  fieldless `CopyImprintedCardAndMayCastCopyEffect.otherExiledCard()`, which reads the spell's name as it resolves;
- Bucket List: "whenever you cast a spell of a type showing on Bucket List" is `Triggers.youCastSpell()
  .withStackEntryFilter(new StackEntryHasTypeShowingOnSourceTrackerPredicate(types))`, and the effect
  `TrackCastSpellCardTypesEffect(types)` reads the spell's types from the triggering spell.

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17zb3-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"
E = "model.effect."
F = "model.filter."
C = "model.condition."
T = "model.trigger."

CARDS = {
    "HarnessTheStorm": (
        [("""        addTrigger(Triggers.youCastSpell(),
                new CastSameNameCardFromGraveyardOnSpellCastEffect());""",
          """        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY))))
                        .castFrom(Zone.HAND),
                new CastTargetInstantOrSorceryFromGraveyardEffect(GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                        false, false, new CardSharesNameWithTriggeringSpellPredicate()));""")],
        [F + "CardAnyOfPredicate", F + "CardTypePredicate", "model.CardType", "model.Zone",
         E + "CastTargetInstantOrSorceryFromGraveyardEffect", "model.GraveyardSearchScope",
         F + "CardSharesNameWithTriggeringSpellPredicate", "java:java.util.List"],
        [E + "CastSameNameCardFromGraveyardOnSpellCastEffect"]),
    "TooferKeeperOfTheFullGrip": (
        [("""        addTrigger(Triggers.youCastSpell(), new CardAdvantageSpellCastTriggerEffect());""",
          """        addTrigger(Triggers.yourSpellCausesYouToGainCardAdvantage(),
                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT));""")],
        [E + "LoseLifeEffect", E + "LoseLifeRecipient"],
        [E + "CardAdvantageSpellCastTriggerEffect"]),
    "SpellweaverHelix": (
        [("""        addTrigger(Triggers.playerCastsSpell(), new SpellweaverHelixTriggerEffect());""",
          """        addTrigger(Triggers.playerCastsSpell()
                        .withStackEntryFilter(new StackEntryNotPredicate(new StackEntryIsCopyPredicate(true))),
                TriggerOptions.DEFAULT.withInterveningIf(new TriggeringSpellMatches(
                        new StackEntrySharesNameWithCardExiledWithSourcePredicate())),
                new MayEffect(CopyImprintedCardAndMayCastCopyEffect.otherExiledCard(),
                        "You may copy the other exiled card and cast it without paying its mana cost?"));""")],
        [F + "StackEntryNotPredicate", F + "StackEntryIsCopyPredicate", T + "TriggerOptions",
         C + "TriggeringSpellMatches", F + "StackEntrySharesNameWithCardExiledWithSourcePredicate",
         E + "CopyImprintedCardAndMayCastCopyEffect"],
        [E + "SpellweaverHelixTriggerEffect"]),
    "BucketList": (
        [("""        addTrigger(Triggers.youCastSpell(), new TrackCastSpellCardTypesEffect(Set.of(
                CardType.ARTIFACT, CardType.CREATURE, CardType.ENCHANTMENT, CardType.INSTANT, CardType.SORCERY)));""",
          """        Set<CardType> trackedTypes = Set.of(
                CardType.ARTIFACT, CardType.CREATURE, CardType.ENCHANTMENT, CardType.INSTANT, CardType.SORCERY);
        addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryHasTypeShowingOnSourceTrackerPredicate(trackedTypes)),
                new TrackCastSpellCardTypesEffect(trackedTypes));""")],
        [F + "StackEntryHasTypeShowingOnSourceTrackerPredicate"],
        []),
}

GONE = ["CastSameNameCardFromGraveyardOnSpellCastEffect", "CardAdvantageSpellCastTriggerEffect",
        "SpellweaverHelixTriggerEffect"]


def import_line(fqcn):
    return f"import {fqcn[5:]};" if fqcn.startswith("java:") else f"import {PKG}.{fqcn};"


def add_import(text, fqcn):
    line = import_line(fqcn)
    if line in text:
        return text
    imports = list(re.finditer(r"^import [^;]+;$", text, re.M))
    pos = imports[0].start()
    return text[:pos] + line + "\n" + text[pos:]


def remove_unused_import(text, fqcn):
    line = import_line(fqcn) + "\n"
    simple = line.rsplit(".", 1)[1].rstrip(";\n")
    if line in text and not re.search(r"\b" + simple + r"\b", text.replace(line, "")):
        text = text.replace(line, "")
    return text


def sort_imports(text):
    """Keeps the project's import block sorted: the package's imports first, then java.*."""
    imports = list(re.finditer(r"^import [^;]+;$", text, re.M))
    if not imports:
        return text
    start, end = imports[0].start(), imports[-1].end()
    block = text[start:end]
    if re.search(r"^(?!import |\s*$).+", block, re.M):
        return text
    lines = [m.group(0) for m in imports]
    ours = sorted(l for l in lines if not l.startswith("import java"))
    java = sorted(l for l in lines if l.startswith("import java"))
    joined = "\n".join(ours) + ("\n\n" + "\n".join(java) if java and ours else "\n".join(java))
    return text[:start] + joined + text[end:]


def convert_file(path, name):
    if name not in CARDS:
        return
    text = path.read_text()
    original = text
    edits, imports, removed = CARDS[name]
    for old, new in edits:
        if old in text:
            text = text.replace(old, new)
        elif not new or new not in text:
            if not new and not any(re.search(r"\b" + gone + r"\b", text) for gone in GONE):
                continue
            raise SystemExit(f"{name}: expected text not found:\n{old}")
    if text == original:
        return
    for fqcn in imports:
        text = add_import(text, fqcn)
    for fqcn in removed:
        text = remove_unused_import(text, fqcn)
    text = sort_imports(text)
    text = re.sub(r"\n{3,}", "\n\n", text)
    for gone in GONE:
        if re.search(r"\b" + gone + r"\b", text):
            raise SystemExit(f"{name}: still names {gone}")
    path.write_text(text)
    print(f"converted {name}")


def main():
    modules = sorted(set("abcdefghijklmnopqrstuvwxyz")) + ["shared"] if sys.argv[1:] == ["all"] else sys.argv[1:]
    if not modules:
        raise SystemExit(__doc__)
    for module in modules:
        root = ROOT / f"magical-vibes-card/{module}/src/main/java"
        if not root.exists():
            continue
        for path in sorted(root.rglob("*.java")):
            convert_file(path, path.stem)


if __name__ == "__main__":
    main()
