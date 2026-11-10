#!/usr/bin/env python3
"""Step 15, run 17z13e5b: the cards say in their triggers what the graveyard, milled and exiled handlers judged.

Run per letter module: `python3 scripts/step15-run17z13e5b-cards.py c d g m o p y` (no argument: every letter).

- Pia's Revolution: "a nontoken artifact is put into your graveyard" (`Triggers.NONTOKEN_ARTIFACT`, `YOU_OWN`); its
  class handler judged the token and the graveyard's owner.
- Magnetic Mine: "another artifact" (`AllOf(ARTIFACT, Not(IsSourcePermanent))`); its class handler compared the cards.
- Desert Warfare: "a Desert card is put into your graveyard from your hand or library" is one trigger
  (`Triggers.putIntoGraveyardFrom(Set.of(HAND, LIBRARY), AllOf(NONTOKEN, Desert), YOU_OWN)`), triggering once for each
  card; its class handler judged the zone and the effect's Desert filter, which the effect no longer has.
- Oglor, Devoted Assistant: "a creature card is put into your graveyard from your library or hand"
  (`putIntoGraveyardFrom(Set.of(LIBRARY, HAND), …)`); its class handler judged the zone.
- Glowing One: "whenever a player mills a nonland card" triggers once for each card (no `ONE_OR_MORE`); its class
  handler put one ability on the stack for each card.
- Yuna, Grand Summoner: "another permanent you control" (`ANOTHER_YOU_CONTROL`); its class handler judged the
  controller and the collector the source's own move.
- Captain Marvel, Shooting Star: "a creature other than Captain Marvel" (`AllOf(CREATURE, Not(IsSourcePermanent))`);
  the collector judged the source's own exile by the effect's class.
Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
FILTER = "com.github.laxika.magicalvibes.model.filter."


def add_import(text, fqcn):
    """Adds an import in order: a `java.` one after the other `java.` imports (a block of its own if none), any other
    before the first project import that sorts after it."""
    line = f"import {fqcn};\n"
    if line in text:
        return text
    lines = text.split("\n")
    imports = [i for i, current in enumerate(lines) if current.startswith("import ")]
    java = [i for i in imports if lines[i].startswith("import java.")]
    project = [i for i in imports if not lines[i].startswith("import java.")]
    if fqcn.startswith("java."):
        if java:
            after = [i for i in java if lines[i] < line.rstrip("\n")]
            index = (after[-1] + 1) if after else java[0]
            lines.insert(index, line.rstrip("\n"))
        else:
            lines[project[-1] + 1:project[-1] + 1] = ["", line.rstrip("\n")]
    else:
        later = [i for i in project if lines[i] > line.rstrip("\n")]
        lines.insert(later[0] if later else project[-1] + 1, line.rstrip("\n"))
    return "\n".join(lines)


def drop_import_if_unused(text, fqcn):
    simple = fqcn.rsplit(".", 1)[1]
    line = f"import {fqcn};\n"
    body = text.replace(line, "")
    if line in text and simple not in body:
        return body
    return text


def edit(letter, name, replacements, imports=(), unused=()):
    path = ROOT / f"magical-vibes-card/{letter}/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"
    text = path.read_text()
    for old, new in replacements:
        if new in text:
            continue
        assert old in text, (name, old)
        text = text.replace(old, new)
    for fqcn in imports:
        text = add_import(text, fqcn)
    for fqcn in unused:
        text = drop_import_if_unused(text, fqcn)
    path.write_text(text)


ANOTHER = ("new PermanentAllOfPredicate(List.of(Triggers.{0},\n"
           "                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())))")

CARDS = {
    "p": [("PiasRevolution", [(
        "Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY)",
        "Triggers.dies(Triggers.NONTOKEN_ARTIFACT, ObjectRelation.YOU_OWN)")], (), ())],
    "m": [("MagneticMine", [(
        "addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY),",
        "addTrigger(Triggers.dies(" + ANOTHER.format("ARTIFACT") + ", ObjectRelation.ANY),")],
        (FILTER + "PermanentAllOfPredicate", FILTER + "PermanentNotPredicate",
         FILTER + "PermanentIsSourcePermanentPredicate", "java.util.List"), ())],
    "c": [("CaptainMarvelShootingStar", [(
        "addTrigger(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), Triggers.CREATURE, ObjectRelation.ANY),",
        "addTrigger(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), " + ANOTHER.format("CREATURE")
        + ",\n                        ObjectRelation.ANY),")],
        (FILTER + "PermanentAllOfPredicate", FILTER + "PermanentNotPredicate",
         FILTER + "PermanentIsSourcePermanentPredicate", "java.util.List"), ())],
    "y": [("YunaGrandSummoner", [(
        "Triggers.dies(new PermanentHasCountersPredicate(CounterType.ANY), ObjectRelation.ANY)",
        "Triggers.dies(new PermanentHasCountersPredicate(CounterType.ANY), ObjectRelation.ANOTHER_YOU_CONTROL)")],
        (), ())],
    "g": [("GlowingOne", [(
        "addTrigger(new TriggerCondition(Triggers.mills(Triggers.NONLAND, ObjectRelation.ANY)),\n"
        "                TriggerOptions.ONE_OR_MORE, new GainLifeEffect(1));",
        "addTrigger(Triggers.mills(Triggers.NONLAND, ObjectRelation.ANY), new GainLifeEffect(1));")],
        (), ("com.github.laxika.magicalvibes.model.trigger.TriggerCondition",
             "com.github.laxika.magicalvibes.model.trigger.TriggerOptions"))],
    "o": [("OglorDevotedAssistant", [(
        "Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_OWN)",
        "Triggers.putIntoGraveyardFrom(Set.of(Zone.LIBRARY, Zone.HAND), Triggers.NONTOKEN_CREATURE,\n"
        "                        ObjectRelation.YOU_OWN)")],
        ("com.github.laxika.magicalvibes.model.Zone", "java.util.Set"), ())],
    "d": [("DesertWarfare", [(
        """        CardSubtypePredicate desertCard = new CardSubtypePredicate(CardSubtype.DESERT);
        RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect returnDesert =
                new RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect(desertCard);

        // Whenever you sacrifice a Desert, or a Desert card is put into your graveyard from your
        // hand or library, return that card to the battlefield under your control at the next end step.
        addTrigger(Triggers.sacrificed(
                        new PermanentHasSubtypePredicate(CardSubtype.DESERT),
                        ObjectRelation.YOU_CONTROL),
                returnDesert);
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.ANOTHER_YOU_OWN),
                returnDesert);
        addTrigger(new TriggerCondition(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN,
                ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE, returnDesert);
""",
        """        RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect returnDesert =
                new RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect();

        // Whenever you sacrifice a Desert and whenever a Desert card is put into your graveyard from your
        // hand or library, put that card onto the battlefield under your control at the beginning of your
        // next end step. "That card" is the stack entry's triggering card; each Desert card triggers once.
        addTrigger(Triggers.sacrificed(
                        new PermanentHasSubtypePredicate(CardSubtype.DESERT),
                        ObjectRelation.YOU_CONTROL),
                returnDesert);
        addTrigger(Triggers.putIntoGraveyardFrom(Set.of(Zone.HAND, Zone.LIBRARY),
                        new PermanentAllOfPredicate(List.of(Triggers.NONTOKEN,
                                new PermanentHasSubtypePredicate(CardSubtype.DESERT))),
                        ObjectRelation.YOU_OWN),
                returnDesert);
""")],
        (FILTER + "PermanentAllOfPredicate",),
        (FILTER + "CardSubtypePredicate", "com.github.laxika.magicalvibes.model.trigger.TriggerCondition"))],
}


def main():
    letters = sys.argv[1:] or sorted(CARDS)
    for letter in letters:
        for name, replacements, imports, unused in CARDS.get(letter, []):
            edit(letter, name, replacements, imports, unused)


if __name__ == "__main__":
    main()
