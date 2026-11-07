#!/usr/bin/env python3
"""Step 15 run 17f of effect-slot-refactor.md: the enters-side event bindings read the trigger event.

Idempotent; run per letter module (``--module h``) or with no argument for every module this run touches.

- Hulkling, Evolving Adaptive, Sharp-Eyed Rookie: "whenever another creature you control enters, if it has greater
  power or toughness than this creature, put a counter on this creature [and investigate]" is the enters trigger, the
  intervening "if" ``TriggeringPermanentHasGreaterPowerOrToughnessThanSource`` and existing effects, where the
  class-keyed collector judged ``EvolveTriggerEffect`` and bound the entering creature's stats into the entry.
- Pollywog Prodigy: its printed evolve is the keyword, which the keyword collector triggers; the authored copy of it,
  which made the ability trigger twice, goes (a rules fix, CR 702.100d).
- Champion of the Hareish: ``BuddyListGrowOrWriteEffect`` reads the stack entry's triggering creature as the ability
  resolves, where the collector bound its creature types as it triggered.
- Kodama of the East Tree: "if it wasn't put onto the battlefield with this ability" is the trigger's filter
  (``PermanentPutOntoBattlefieldBySourcePredicate``), where the collector read the put effect's flag.
- Oran-Rief Hydra, Emeria Shepherd: "if that land is a Forest (Plains) ... instead" is one ability whose
  ``ConditionalReplacementEffect`` reads the triggering land as the ability resolves (as it last existed once it has
  left), where each branch was an ability of its own judged as the land entered.
"""
import argparse
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
PKG = "src/main/java/com/github/laxika/magicalvibes/cards"
M = "com.github.laxika.magicalvibes.model"

EVOLVE_IF = ("TriggerOptions.DEFAULT.withInterveningIf(\n"
             "                        new TriggeringPermanentHasGreaterPowerOrToughnessThanSource())")

CARDS_NEW = {
    ("h", "HulklingBurgeoningBruiser"): (
        [f"{M}.Card", f"{M}.condition.TriggeringPermanentHasGreaterPowerOrToughnessThanSource",
         f"{M}.effect.PutCountersOnSourceEffect", f"{M}.trigger.ObjectRelation", f"{M}.trigger.TriggerOptions",
         f"{M}.trigger.Triggers"],
        f"""        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                {EVOLVE_IF},
                new PutCountersOnSourceEffect(1, 1, 1));
"""),
    ("e", "EvolvingAdaptive"): (
        [f"{M}.Card", f"{M}.CounterType", f"{M}.amount.CountersOnSource", f"{M}.amount.Fixed",
         f"{M}.condition.TriggeringPermanentHasGreaterPowerOrToughnessThanSource",
         f"{M}.effect.DynamicStaticBoostEffect", f"{M}.effect.EnterWithCountersEffect", f"{M}.effect.GrantScope",
         f"{M}.effect.PutCountersOnSelfEffect", f"{M}.trigger.ObjectRelation", f"{M}.trigger.TriggerOptions",
         f"{M}.trigger.Triggers"],
        f"""        addStaticEffect(new EnterWithCountersEffect(CounterType.OIL, new Fixed(1)));
        addStaticEffect(new DynamicStaticBoostEffect(
                new CountersOnSource(CounterType.OIL),
                new CountersOnSource(CounterType.OIL),
                GrantScope.SELF));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                {EVOLVE_IF},
                new PutCountersOnSelfEffect(CounterType.OIL));
"""),
    ("s", "SharpEyedRookie"): (
        [f"{M}.Card", f"{M}.condition.TriggeringPermanentHasGreaterPowerOrToughnessThanSource",
         f"{M}.effect.CreateTokenEffect", f"{M}.effect.PutCountersOnSourceEffect", f"{M}.effect.SequenceEffect",
         f"{M}.trigger.ObjectRelation", f"{M}.trigger.TriggerOptions", f"{M}.trigger.Triggers"],
        f"""        // "Whenever a creature you control enters": this creature entering never has greater power or toughness
        // than itself. The intervening "if" is checked once as the ability resolves, before the counter and the Clue.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                {EVOLVE_IF},
                SequenceEffect.of(new PutCountersOnSourceEffect(1, 1, 1), CreateTokenEffect.ofClueToken(1)));
"""),
    ("p", "PollywogProdigy"): (
        [f"{M}.Card", f"{M}.CardType", f"{M}.effect.DrawCardEffect", f"{M}.filter.CardAllOfPredicate",
         f"{M}.filter.CardManaValueLessThanSourcePowerPredicate", f"{M}.filter.CardNotPredicate",
         f"{M}.filter.CardTypePredicate", f"{M}.trigger.Triggers", "", "java.util.List"],
        """        // Evolve is the keyword, which the ally-creature entry collector triggers.
        addTrigger(Triggers.opponentCastsSpell()
                        .withFilter(new CardAllOfPredicate(List.of(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                                new CardManaValueLessThanSourcePowerPredicate()
                        ))),
                new DrawCardEffect());
"""),
    ("c", "ChampionOfTheHareish"): (
        [f"{M}.Card", f"{M}.CardSubtype", f"{M}.effect.BuddyListGrowOrWriteEffect",
         f"{M}.effect.BuddyListOnEnterEffect", f"{M}.trigger.ObjectRelation", f"{M}.trigger.Triggers", "",
         "java.util.List"],
        """        addStaticEffect(new BuddyListOnEnterEffect(List.of(CardSubtype.RABBIT, CardSubtype.SOLDIER)));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new BuddyListGrowOrWriteEffect());
"""),
    ("k", "KodamaOfTheEastTree"): (
        [f"{M}.Card", f"{M}.effect.MayEffect", f"{M}.effect.PutCardToBattlefieldEffect",
         f"{M}.filter.CardIsPermanentPredicate", f"{M}.filter.PermanentNotPredicate",
         f"{M}.filter.PermanentPutOntoBattlefieldBySourcePredicate", f"{M}.trigger.ObjectRelation",
         f"{M}.trigger.Triggers"],
        """        // "If it wasn't put onto the battlefield with this ability" can't change once it entered: the filter.
        addTrigger(Triggers.enters(new PermanentNotPredicate(new PermanentPutOntoBattlefieldBySourcePredicate()),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(
                        new PutCardToBattlefieldEffect(new CardIsPermanentPredicate(), "permanent")
                                .boundedByEventValue(),
                        "Put a permanent card with equal or lesser mana value from your hand onto the battlefield?"));
"""),
    ("o", "OranRiefHydra"): (
        [f"{M}.Card", f"{M}.CardSubtype", f"{M}.CounterType", f"{M}.condition.TriggeringPermanentHasSubtype",
         f"{M}.effect.ConditionalReplacementEffect", f"{M}.effect.PutCountersOnSelfEffect",
         f"{M}.trigger.ObjectRelation", f"{M}.trigger.Triggers"],
        """        // Landfall — Whenever a land you control enters, put a +1/+1 counter on this creature.
        // If that land is a Forest, put two +1/+1 counters on this creature instead. The land is read as the
        // ability resolves, as it last existed on the battlefield once it has left (CR 608.2h).
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new ConditionalReplacementEffect(new TriggeringPermanentHasSubtype(CardSubtype.FOREST),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)));
"""),
    ("e", "EmeriaShepherd"): (
        [f"{M}.Card", f"{M}.CardSubtype", f"{M}.CardType", f"{M}.GraveyardChoiceDestination",
         f"{M}.GraveyardSearchScope", f"{M}.condition.TriggeringPermanentHasSubtype",
         f"{M}.effect.ConditionalReplacementEffect", f"{M}.effect.MayEffect",
         f"{M}.effect.ReturnCardFromGraveyardEffect", f"{M}.filter.CardAllOfPredicate",
         f"{M}.filter.CardIsPermanentPredicate", f"{M}.filter.CardNotPredicate", f"{M}.filter.CardPredicate",
         f"{M}.filter.CardTypePredicate", f"{M}.filter.GraveyardCardPredicateTargetFilter",
         f"{M}.trigger.ObjectRelation", f"{M}.trigger.Triggers", "", "java.util.List"],
        """        CardPredicate nonlandPermanent = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        ReturnCardFromGraveyardEffect returnToHand = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(nonlandPermanent)
                .targetGraveyard(true)
                .build();
        ReturnCardFromGraveyardEffect returnToBattlefield = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(nonlandPermanent)
                .targetGraveyard(true)
                .chooseAuraAttachment(true)
                .build();
        MayEffect mayReturnToHand = new MayEffect(returnToHand, "Return that card to your hand?");

        // Landfall - Whenever a land you control enters, you may return target nonland permanent card
        // from your graveyard to your hand. If that land is a Plains, you may return that nonland
        // permanent card to the battlefield instead. The land is read as the ability resolves, as it last
        // existed on the battlefield once it has left (CR 608.2h).
        target(new GraveyardCardPredicateTargetFilter(
                nonlandPermanent, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                .addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                        new ConditionalReplacementEffect(new TriggeringPermanentHasSubtype(CardSubtype.PLAINS),
                                mayReturnToHand,
                                new MayEffect(returnToBattlefield,
                                        "Return that card to the battlefield instead?", mayReturnToHand)));
"""),
}

DONE = {
    "HulklingBurgeoningBruiser": "TriggeringPermanentHasGreaterPowerOrToughnessThanSource()",
    "EvolvingAdaptive": "TriggeringPermanentHasGreaterPowerOrToughnessThanSource()",
    "SharpEyedRookie": "SequenceEffect.of(new PutCountersOnSourceEffect",
    "PollywogProdigy": "Evolve is the keyword",
    "ChampionOfTheHareish": "new BuddyListGrowOrWriteEffect()",
    "KodamaOfTheEastTree": "new PermanentPutOntoBattlefieldBySourcePredicate()",
    "OranRiefHydra": "new ConditionalReplacementEffect(",
    "EmeriaShepherd": "new ConditionalReplacementEffect(",
}


def convert(letter, name, imports, body):
    path = CARDS / letter / PKG / letter / f"{name}.java"
    text = path.read_text()
    if DONE[name] in text:
        print(f"unchanged {path.relative_to(ROOT)}")
        return
    head, sep, rest = text.partition("@CardRegistration")
    if not sep:
        sys.exit(f"{path}: no @CardRegistration")
    package = head.splitlines()[0]
    import_lines = ["import com.github.laxika.magicalvibes.cards.CardRegistration;"]
    import_lines += [f"import {imp};" if imp else "" for imp in imports]
    registrations, _, _ = rest.partition(f"public class {name} extends Card {{")
    constructor = f"    public {name}() {{\n"
    if constructor not in text:
        sys.exit(f"{path}: no constructor")
    new_text = (f"{package}\n\n" + "\n".join(import_lines) + "\n\n" + sep + registrations
                + f"public class {name} extends Card {{\n\n" + constructor + body + "    }\n}\n")
    path.write_text(new_text)
    print(f"converted {path.relative_to(ROOT)}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--module", help="letter module to convert (default: all this run touches)")
    args = parser.parse_args()
    for (letter, name), (imports, body) in CARDS_NEW.items():
        if args.module is None or args.module == letter:
            convert(letter, name, imports, body)


if __name__ == "__main__":
    main()
