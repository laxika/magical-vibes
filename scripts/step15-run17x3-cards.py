#!/usr/bin/env python3
"""Step 15, run 17x3: the enter family's values read the stack entry's triggering permanent.

Rewrites the card classes whose enter-trigger effect a collector rebuilt with a value of the entering creature bound
in, or swapped for another class:

- Guardian Project: "if it doesn't have the same name as another creature you control or a creature card in your
  graveyard" is the intervening "if" `TriggeringPermanentMatches(not PermanentSharesNameWithControlledCreatureOr
  GraveyardCreaturePredicate)`, with the existing `DrawCardEffect` (replacing `DrawCardIfEnteringCreatureHasUnique
  NameEffect`, which the collector rebuilt with the creature's name and card id).
- Wolverine Riders, Trostani, Selesnya's Voice, Righteous Valkyrie, Angelic Chorus, Abattoir Ghoul, Orchard Warden:
  "gain life equal to its / that creature's toughness" is `GainLifeEffect(new TriggeringPermanentToughness())`;
  Verdant Sun's Avatar the same with its two trigger conditions one "this or another creature you control enters";
  Archon of Redemption's "that creature's power" `GainLifeEffect(new TriggeringPermanentPower())` (replacing
  `GainLifeEqualToToughnessEffect` / `GainLifeEqualToPowerEffect`).
- Hamletback Goliath: "you may put X +1/+1 counters on this creature, where X is that creature's power" is
  `MayEffect(PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE, new TriggeringPermanentPower()))`.
- Quickbeast Amulet: "intensifies by X, where X is that creature's power" is
  `IntensifySourceCardByAmountEffect(new TriggeringPermanentPower())`.
- Cream of the Crop: "you may look at the top X cards ..., where X is that creature's power" is
  `MayEffect(LookAtTopCardsEffect.putOneOnTopRestOnBottom(new TriggeringPermanentPower()))`.
- Wild Pair: "if you cast it from your hand" is the intervening "if" `TriggeringPermanentMatches(cast from hand by
  you)`, its "you may" the card's own `MayEffect` around the fieldless `SearchLibraryForCreatureWithSameTotalPower
  ToughnessEffect`.

Idempotent: a file already converted is left alone. Per letter module.

Usage: step15-run17x3-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

TOUGHNESS_OLD = "new GainLifeEqualToToughnessEffect()"
TOUGHNESS_NEW = "new GainLifeEffect(new TriggeringPermanentToughness())"
TOUGHNESS = ([(TOUGHNESS_OLD, TOUGHNESS_NEW)], ["model.amount.TriggeringPermanentToughness", "model.effect.GainLifeEffect"],
             ["model.effect.GainLifeEqualToToughnessEffect"])

# card -> (letter, [(old, new)], imports to add, imports to remove)
CARDS = {
    "GuardianProject": ("g", [
        ("""        addTrigger(Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new DrawCardIfEnteringCreatureHasUniqueNameEffect());""",
         """        // Whenever another nontoken creature you control enters, if it doesn't have the same name as another
        // creature you control or a creature card in your graveyard, draw a card. The intervening "if" is judged as
        // the creature enters and again as the ability resolves, against the creature as it last existed if it has
        // left (CR 603.4, CR 608.2h).
        addTrigger(Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentMatches(new PermanentNotPredicate(
                        new PermanentSharesNameWithControlledCreatureOrGraveyardCreaturePredicate()))),
                new DrawCardEffect());"""),
    ], ["model.condition.TriggeringPermanentMatches", "model.effect.DrawCardEffect",
        "model.filter.PermanentNotPredicate",
        "model.filter.PermanentSharesNameWithControlledCreatureOrGraveyardCreaturePredicate",
        "model.trigger.TriggerOptions"],
        ["model.effect.DrawCardIfEnteringCreatureHasUniqueNameEffect"]),
    "WolverineRiders": ("w",) + TOUGHNESS,
    "TrostaniSelesnyasVoice": ("t",) + TOUGHNESS,
    "RighteousValkyrie": ("r",) + TOUGHNESS,
    "AngelicChorus": ("a",) + TOUGHNESS,
    "AbattoirGhoul": ("a",) + TOUGHNESS,
    "OrchardWarden": ("o",) + TOUGHNESS,
    "VerdantSunsAvatar": ("v", [
        ("""        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.enters(ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(new GainLifeEqualToToughnessEffect()))));""",
         """        // Whenever this creature or another creature you control enters, you gain life equal to that creature's
        // toughness, read as the ability resolves (CR 608.2h).
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                new GainLifeEffect(new TriggeringPermanentToughness()));"""),
    ], ["model.amount.TriggeringPermanentToughness", "model.effect.GainLifeEffect"],
        ["model.EffectRegistration", "model.TriggeredAbility", "model.effect.GainLifeEqualToToughnessEffect",
         "model.trigger.TriggerCondition", "model.trigger.TriggerOptions", "java:java.util.List"]),
    "ArchonOfRedemption": ("a", [
        ("new GainLifeEqualToPowerEffect()", "new GainLifeEffect(new TriggeringPermanentPower())"),
    ], ["model.amount.TriggeringPermanentPower", "model.effect.GainLifeEffect"],
        ["model.effect.GainLifeEqualToPowerEffect"]),
    "HamletbackGoliath": ("h", [
        ("""                new PutCountersOnSourceEqualToEnteringPowerEffect(1, 1, true));""",
         """                new MayEffect(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new TriggeringPermanentPower()),
                        "Put X +1/+1 counters on this creature, where X is that creature's power?"));"""),
    ], ["model.CounterType", "model.amount.TriggeringPermanentPower", "model.effect.MayEffect",
        "model.effect.PutCountersOnSelfEffect"],
        ["model.effect.PutCountersOnSourceEqualToEnteringPowerEffect"]),
    "QuickbeastAmulet": ("q", [
        ("new IntensifySourceByEnteringPowerEffect()",
         "new IntensifySourceCardByAmountEffect(new TriggeringPermanentPower())"),
    ], ["model.amount.TriggeringPermanentPower", "model.effect.IntensifySourceCardByAmountEffect"],
        ["model.effect.IntensifySourceByEnteringPowerEffect"]),
    "CreamOfTheCrop": ("c", [
        ("""                new LookAtTopCardsEqualToEnteringPowerPutOneOnTopRestOnBottomEffect());""",
         """                new MayEffect(LookAtTopCardsEffect.putOneOnTopRestOnBottom(new TriggeringPermanentPower()),
                        "Look at the top X cards of your library, where X is that creature's power?"));"""),
    ], ["model.amount.TriggeringPermanentPower", "model.effect.LookAtTopCardsEffect", "model.effect.MayEffect"],
        ["model.effect.LookAtTopCardsEqualToEnteringPowerPutOneOnTopRestOnBottomEffect"]),
    "WildPair": ("w", [
        ("""        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new SearchLibraryForCreatureWithSameTotalPowerToughnessEffect());""",
         """        // Whenever a creature enters, if you cast it from your hand, you may search your library for a creature card
        // with the same total power and toughness and put it onto the battlefield. If you do, shuffle. The intervening
        // "if" is judged as the creature enters and again as the ability resolves (CR 603.4); the total is read as it
        // resolves (CR 608.2h).
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentMatches(new PermanentAllOfPredicate(
                        List.of(new PermanentCastFromZonePredicate(Zone.HAND),
                                new PermanentCastBySourceControllerThisTurnPredicate())))),
                new MayEffect(new SearchLibraryForCreatureWithSameTotalPowerToughnessEffect(),
                        "Search your library for a creature card with the same total power and toughness?"));"""),
    ], ["model.Zone", "model.condition.TriggeringPermanentMatches", "model.effect.MayEffect",
        "model.filter.PermanentAllOfPredicate", "model.filter.PermanentCastBySourceControllerThisTurnPredicate",
        "model.filter.PermanentCastFromZonePredicate", "model.trigger.TriggerOptions", "java:java.util.List"], []),
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
