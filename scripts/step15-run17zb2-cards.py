#!/usr/bin/env python3
"""Step 15, run 17zb2: the cast collector's class-keyed handlers for Loki, God of Lies, Spellweaver Volute, Shiko and
Narset, Unified, Vesuvan Duplimancy and Orvar, the All-Form are filtered cast triggers with existing effects reading the
stack entry's triggering spell.

`SpellCastTriggerCollectorService` read four markers by class and built the effect that went on the stack from the
event. Each card now writes the ability:

- Loki, God of Lies: "whenever you cast a spell that targets only a single creature" is
  `Triggers.youCastSpell().withStackEntryFilter(new StackEntryTargetsOnlySingleCreaturePredicate())`; "gain control of
  that creature until end of turn. If it's your turn, untap that creature and it gains haste until end of turn" is
  `GainControlOfAllPermanentsMatchingEffect(new PermanentIsTargetOfTriggeringSpellPredicate(), END_OF_TURN, …)` with
  the `ControllerTurn` untap-and-haste rider;
- Spellweaver Volute: "whenever you cast a sorcery spell" is `Triggers.youCastSpell().withFilter(sorcery)` and the
  `MayEffect` of the fieldless `CopyEnchantedInstantAndMayCastCopyEffect()`, which reads the enchanted card from the
  Aura as it resolves;
- Shiko and Narset, Unified: flurry is `Triggers.youCastSpell().nthThisTurn(2)`; "copy that spell if it targets a
  permanent or player" `ConditionalEffect(new TriggeringSpellMatches(…), new CopyTriggeringSpellEffect())`, "if you
  don't copy a spell this way, draw a card" `ConditionalEffect(new NotCondition(new SpellCopiedThisWay()),
  new DrawCardEffect())`;
- Vesuvan Duplimancy: the spell condition moves from the effect to `withStackEntryFilter`, and the effect is
  `CreateTokenCopyOfTargetedSpellPermanentEffect(copyEffect)`;
- Orvar, the All-Form: "an instant or sorcery spell, if it targets one or more other permanents you control" is
  `withFilter(instant or sorcery).withStackEntryFilter(targets another permanent you control)`; the effect, which
  checks the targets again as it resolves, keeps its no-argument form.

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17zb2-cards.py <letter> [<letter> ...]   (or "all")
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
    "LokiGodOfLies": (
        [("""        addTrigger(Triggers.youCastSpell(),
                new GainControlOfTargetCreatureWhenSingleTargetSpellCastEffect());""",
          """        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryTargetsOnlySingleCreaturePredicate()),
                new GainControlOfAllPermanentsMatchingEffect(new PermanentIsTargetOfTriggeringSpellPredicate(),
                        ControlDuration.END_OF_TURN,
                        new ConditionalEffect(new ControllerTurn(), SequenceEffect.of(
                                new UntapPermanentsEffect(TapUntapScope.TARGET),
                                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)))));""")],
        [F + "StackEntryTargetsOnlySingleCreaturePredicate", E + "GainControlOfAllPermanentsMatchingEffect",
         F + "PermanentIsTargetOfTriggeringSpellPredicate", E + "ControlDuration", E + "ConditionalEffect",
         C + "ControllerTurn", E + "SequenceEffect", E + "UntapPermanentsEffect", E + "TapUntapScope",
         E + "GrantKeywordEffect", "model.Keyword", E + "GrantScope"],
        [E + "GainControlOfTargetCreatureWhenSingleTargetSpellCastEffect"]),
    "SpellweaverVolute": (
        [("""        addTrigger(Triggers.playerCastsSpell(), new SpellweaverVoluteTriggerEffect());""",
          """        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.SORCERY)),
                new MayEffect(new CopyEnchantedInstantAndMayCastCopyEffect(),
                        "You may cast a copy of the enchanted instant without paying its mana cost?"));""")],
        [E + "MayEffect", E + "CopyEnchantedInstantAndMayCastCopyEffect"],
        [E + "SpellweaverVoluteTriggerEffect"]),
    "ShikoAndNarsetUnified": (
        [("""        addTrigger(Triggers.youCastSpell(), new FlurryCopyOrDrawTriggerEffect());""",
          """        addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                new ConditionalEffect(new TriggeringSpellMatches(new StackEntryAnyOfPredicate(List.of(
                        new StackEntryTargetsPermanentPredicate(new PermanentTruePredicate()),
                        new StackEntryTargetsAnyPlayerPredicate()))),
                        new CopyTriggeringSpellEffect()),
                new ConditionalEffect(new NotCondition(new SpellCopiedThisWay()), new DrawCardEffect()));""")],
        [E + "ConditionalEffect", C + "TriggeringSpellMatches", F + "StackEntryAnyOfPredicate",
         F + "StackEntryTargetsPermanentPredicate", F + "PermanentTruePredicate",
         F + "StackEntryTargetsAnyPlayerPredicate", E + "CopyTriggeringSpellEffect", C + "NotCondition",
         C + "SpellCopiedThisWay", E + "DrawCardEffect", "java:java.util.List"],
        [E + "FlurryCopyOrDrawTriggerEffect"]),
    "VesuvanDuplimancy": (
        [("""        addTrigger(Triggers.youCastSpell(),
                new CreateTokenCopyOfTargetedSpellPermanentEffect(triggerCondition,
                        CreateTokenCopyOfTargetPermanentEffect.nonLegendary(
                                List.of(), Set.of(), null, null, Map.of())));""",
          """        addTrigger(Triggers.youCastSpell().withStackEntryFilter(triggerCondition),
                new CreateTokenCopyOfTargetedSpellPermanentEffect(
                        CreateTokenCopyOfTargetPermanentEffect.nonLegendary(
                                List.of(), Set.of(), null, null, Map.of())));""")],
        [],
        []),
    "OrvarTheAllForm": (
        [("""        addTrigger(Triggers.youCastSpell(),
                new CreateTokenCopyOfTargetedSpellPermanentEffect());""",
          """        PermanentPredicate otherPermanentYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                new PermanentControlledBySourceControllerPredicate()));
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY))))
                        .withStackEntryFilter(new StackEntryTargetsPermanentPredicate(otherPermanentYouControl)),
                new CreateTokenCopyOfTargetedSpellPermanentEffect());""")],
        [F + "CardAnyOfPredicate", F + "CardTypePredicate", "model.CardType", F + "StackEntryTargetsPermanentPredicate",
         F + "PermanentAllOfPredicate", F + "PermanentNotPredicate", F + "PermanentIsSourcePermanentPredicate",
         F + "PermanentControlledBySourceControllerPredicate", F + "PermanentPredicate", "java:java.util.List"],
        []),
}

GONE = ["GainControlOfTargetCreatureWhenSingleTargetSpellCastEffect", "SpellweaverVoluteTriggerEffect",
        "FlurryCopyOrDrawTriggerEffect"]


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
