#!/usr/bin/env python3
"""Step 15, run 17z3: the attack and enlist records and the triggering permanent's mana value are written as
abilities whose existing effects read the stack entry.

`CombatAttackService` read Garruk, Apex Predator's emblem marker by class and built the boost from its fields; four
class-keyed collectors rode the "an attacking creature's ability triggers" event to find an enlistment (Guardian of
New Benalia, Goblin Morale Sergeant, Benalish Knight-Counselor, Aradesh, the Founder); the enter and ally-attack
collectors baked the triggering permanent's mana value into the entry for `TriggeringPermanentManaValueEffect`. Each
card now writes the ability:

- Garruk, Apex Predator: the emblem's "whenever a creature attacks you, it gets +5/+5 and gains trample until end of
  turn" is an emblem `TriggeredAbility` on `Triggers.creatureAttacksYou()` with
  `BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, 5, 5, trample)`;
- Guardian of New Benalia: "whenever this creature enlists a creature, scry 2" is `Triggers.enlists()` + `ScryEffect(2)`;
- Goblin Morale Sergeant: "whenever this enlists a nontoken creature, you may conjure a duplicate of that creature" is
  `Triggers.enlists().withFilter(nontoken)` + the `MayEffect` its record put on the stack;
- Benalish Knight-Counselor: `Triggers.enlists()` + its boon effect;
- Aradesh, the Founder: "whenever a creature you control attacks, if it enlisted a creature this combat" is
  `Triggers.creatureYouControlAttacks()` with the intervening "if" `TriggeringPermanentMatches(new
  PermanentEnlistedCreatureThisCombatPredicate())`, its double strike and draw on the triggering permanent;
- Kodama of the East Tree: "with equal or lesser mana value" is `.boundedBy(new TriggeringPermanentManaValue())`;
- Junkyard Scrapper: "with lesser mana value" is `ManaValueBound(new TriggeringPermanentManaValue(), false, -1)`.

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17z3-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"
E = "model.effect."
F = "model.filter."
T = "model.trigger."
C = "model.condition."
A = "model.amount."

CARDS = {
    "GarrukApexPredator": (
        [("""                List.of(new CreateEmblemEffect(
                        List.of(new BoostAttackingCreatureOnAttacksYouEffect(5, 5, Set.of(Keyword.TRAMPLE))),
                        "Whenever a creature attacks you, it gets +5/+5 and gains trample until end of turn.",
                        EmblemRecipient.TARGET_PLAYER)),""",
          """                List.of(new CreateEmblemEffect(List.of(),
                        "Whenever a creature attacks you, it gets +5/+5 and gains trample until end of turn.",
                        EmblemRecipient.TARGET_PLAYER, List.of(),
                        List.of(new TriggeredAbility(Triggers.creatureAttacksYou(),
                                new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING,
                                        new Fixed(5), new Fixed(5), Set.of(Keyword.TRAMPLE)))))),""")],
        [E + "BoostReferencedPermanentEffect", E + "PermanentReference", A + "Fixed", "model.TriggeredAbility",
         T + "Triggers"],
        [E + "BoostAttackingCreatureOnAttacksYouEffect"]),
    "GuardianOfNewBenalia": (
        [("""        addTrigger(Triggers.attackingCreatureYouControlAbilityTriggers(), new ScryWhenThisCreatureEnlistsEffect());""",
          """        addTrigger(Triggers.enlists(), new ScryEffect(2));""")],
        [E + "ScryEffect"],
        [E + "ScryWhenThisCreatureEnlistsEffect"]),
    "GoblinMoraleSergeant": (
        [("""        // Haste and Enlist are loaded from Scryfall; the enlist continuation is handled by combat.
        addTrigger(Triggers.attackingCreatureYouControlAbilityTriggers(),
                new ConjureDuplicateOfEnlistedNontokenCreatureEffect());""",
          """        addTrigger(Triggers.enlists().withFilter(new PermanentNotPredicate(new PermanentIsTokenPredicate())),
                new MayEffect(new ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect(),
                        "Conjure a duplicate of the enlisted creature into the top five cards of your library?"));""")],
        [E + "MayEffect", E + "ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect", F + "PermanentNotPredicate",
         F + "PermanentIsTokenPredicate"],
        [E + "ConjureDuplicateOfEnlistedNontokenCreatureEffect"]),
    "BenalishKnightCounselor": (
        [("""        // Enlist is loaded from Scryfall and handled during attacker declaration.
        addTrigger(Triggers.attackingCreatureYouControlAbilityTriggers(),
                new GrantAdditionalPlusOnePlusOneCounterToNextCreatureSpellEffect());""",
          """        addTrigger(Triggers.enlists(), new GrantAdditionalPlusOnePlusOneCounterToNextCreatureSpellEffect());""")],
        [],
        []),
    "AradeshTheFounder": (
        [("""        addTrigger(Triggers.attackingCreatureYouControlAbilityTriggers(), new AradeshTheFounderEffect());""",
          """        // Whenever a creature you control attacks, if it enlisted a creature this combat, the creature that
        // attacked gains double strike until end of turn. If that creature's power is 4 or greater, draw a card.
        addTrigger(Triggers.creatureYouControlAttacks(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new TriggeringPermanentMatches(new PermanentEnlistedCreatureThisCombatPredicate())),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TRIGGERING_PERMANENT),
                new ConditionalEffect(new TriggeringPermanentPowerAtLeast(4), new DrawCardEffect(1)));""")],
        [E + "GrantKeywordEffect", E + "GrantScope", E + "ConditionalEffect", E + "DrawCardEffect", "model.Keyword",
         C + "TriggeringPermanentMatches", C + "TriggeringPermanentPowerAtLeast",
         F + "PermanentEnlistedCreatureThisCombatPredicate", T + "TriggerOptions"],
        [E + "AradeshTheFounderEffect"]),
    "KodamaOfTheEastTree": (
        [("""                        new PutCardToBattlefieldEffect(new CardIsPermanentPredicate(), "permanent")
                                .boundedByEventValue(),""",
          """                        new PutCardToBattlefieldEffect(new CardIsPermanentPredicate(), "permanent")
                                .boundedBy(new TriggeringPermanentManaValue()),""")],
        [A + "TriggeringPermanentManaValue"],
        []),
    "JunkyardScrapper": (
        [("""                        new ManaValueBound(new EventValue(), false, -1), false, true));""",
          """                        new ManaValueBound(new TriggeringPermanentManaValue(), false, -1), false, true));""")],
        [A + "TriggeringPermanentManaValue"],
        [A + "EventValue"]),
}

GONE = ["BoostAttackingCreatureOnAttacksYouEffect", "ScryWhenThisCreatureEnlistsEffect",
        "ConjureDuplicateOfEnlistedNontokenCreatureEffect", "AradeshTheFounderEffect"]


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
