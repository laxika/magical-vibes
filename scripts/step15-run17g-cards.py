#!/usr/bin/env python3
"""Step 15 run 17g of effect-slot-refactor.md: "it" / "that creature" of a combat damage trigger reads the trigger event.

Idempotent; run per letter module (``--module s``) or with no argument for every module this run touches.

``AllyCombatDamageTriggerEffect`` goes. Its ``boundToDealer`` form made the creature that dealt the combat damage the
stack entry's source, so "put a +1/+1 counter on it" was written as a counter on the source; the collector now records
that creature as the entry's triggering permanent and the effects read it:

- "put a [+1/+1, quest] counter on it" / "put that many +1/+1 counters on it":
  ``PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, ...)`` (White Widow, Stensia Masquerade,
  Sphere Grid, Shaman of the Great Hunt, Rakish Heir, Cazur, Necropolis Regent, Aveline de Grandpré, Duchess,
  Exsanguinator Cavalry, Staff Room);
- Marcus, Mutant Mayor: "if that creature has a +1/+1 counter on it" is ``TriggeringPermanentMatches``;
- Impostor Syndrome: ``CreateTokenCopyOfEnteringPermanentEffect.nonlegendary()`` (the triggering permanent's copy);
- Ikra Shidiqi: ``GainLifeEffect(new TriggeringPermanentToughness())``;
- Gossip's Talent: ``FlickerEffect.flickerTriggeringPermanent()``;
- Staff Room: ``TurnSourceFaceUpEffect.ofTriggeringPermanent()``;
- Aragorn, Hornburg Hero: ``DoublePlusOneCountersOnSourceEffect.ofTriggeringPermanent()``.

Mindblade Render's ``opponentsDealtDamage`` is the "deals combat damage to one of your opponents" trigger filtered to
Warriors, once for the step (``TriggerOptions.ONE_OR_MORE``); Pirated Copy's granted ability names only its trigger,
whose same-name filter the collector judges.
"""
import argparse
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
PKG = "src/main/java/com/github/laxika/magicalvibes/cards"
M = "com.github.laxika.magicalvibes.model"

ON_IT = ("new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING,\n"
         "                        CounterType.PLUS_ONE_PLUS_ONE)")
REF_IMPORTS = [f"{M}.CounterType", f"{M}.effect.PermanentReference", f"{M}.effect.PutCounterOnReferencedPermanentEffect"]

BOUND_COUNTER = "AllyCombatDamageTriggerEffect.boundToDealer(new PutCountersOnSourceEffect(1, 1, 1))"
BOUND_THAT_MANY = ("AllyCombatDamageTriggerEffect.boundToDealer(new PutCountersOnSelfEffect("
                   "CounterType.PLUS_ONE_PLUS_ONE, new EventValue()))")
THAT_MANY = ("new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING,\n"
             "                        CounterType.PLUS_ONE_PLUS_ONE, new EventValue())")

# (letter, class) -> ([(old, new)], imports to add)
CONVERSIONS = {
    ("w", "WhiteWidowYelenaBelova"): ([(BOUND_COUNTER, ON_IT)], REF_IMPORTS),
    ("s", "StensiaMasquerade"): ([(BOUND_COUNTER, ON_IT)], REF_IMPORTS),
    ("s", "SphereGrid"): ([(BOUND_COUNTER, ON_IT)], REF_IMPORTS),
    ("s", "ShamanOfTheGreatHunt"): ([(BOUND_COUNTER, ON_IT)], REF_IMPORTS),
    ("r", "RakishHeir"): ([(BOUND_COUNTER, ON_IT)], REF_IMPORTS),
    ("c", "CazurRuthlessStalker"): ([(BOUND_COUNTER, ON_IT)], REF_IMPORTS),
    ("n", "NecropolisRegent"): ([
        ("""        // Whenever a creature you control deals combat damage to a player, put that many
        // +1/+1 counters on it. The trigger's event value is the damage dealt, and the
        // dealing creature is bound as the stack entry's source so the counters land on it.
""", """        // Whenever a creature you control deals combat damage to a player, put that many
        // +1/+1 counters on it: the damage is the entry's event value, "it" its triggering permanent.
"""),
        (BOUND_THAT_MANY, THAT_MANY)], REF_IMPORTS),
    ("a", "AvelineDeGrandpre"): ([(BOUND_THAT_MANY, THAT_MANY)], REF_IMPORTS),
    ("d", "DuchessWaywardTavernkeep"): ([(
        "AllyCombatDamageTriggerEffect.boundToDealer(new PutCountersOnSelfEffect(CounterType.QUEST))",
        "new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, CounterType.QUEST)")],
        REF_IMPORTS),
    ("e", "ExsanguinatorCavalry"): ([(
        """AllyCombatDamageTriggerEffect.boundToDealer(SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        CreateTokenEffect.ofBloodToken(1))));""",
        """SequenceEffect.of(
                        new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING,
                                CounterType.PLUS_ONE_PLUS_ONE),
                        CreateTokenEffect.ofBloodToken(1)));""")], REF_IMPORTS),
    ("m", "MarcusMutantMayor"): ([(
        """        SourceCounterThreshold hasCounter = new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE);
        // One trigger; the dealing creature's counters are checked as it resolves ("draw a card if
        // that creature has a +1/+1 counter on it. If it doesn't, put a +1/+1 counter on it").
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                AllyCombatDamageTriggerEffect.boundToDealer(SequenceEffect.of(
                        new ConditionalEffect(hasCounter, new DrawCardEffect(1)),
                        new ConditionalEffect(new NotCondition(hasCounter),
                                new PutCountersOnSourceEffect(1, 1, 1)))));""",
        """        TriggeringPermanentMatches hasCounter = new TriggeringPermanentMatches(
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE));
        // One trigger; the dealing creature's counters are checked as it resolves ("draw a card if
        // that creature has a +1/+1 counter on it. If it doesn't, put a +1/+1 counter on it").
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                SequenceEffect.of(
                        new ConditionalEffect(hasCounter, new DrawCardEffect(1)),
                        new ConditionalEffect(new NotCondition(hasCounter),
                                new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING,
                                        CounterType.PLUS_ONE_PLUS_ONE))));""")],
        REF_IMPORTS + [f"{M}.condition.TriggeringPermanentMatches", f"{M}.filter.PermanentHasCountersPredicate"]),
    ("i", "ImpostorSyndrome"): ([(
        "AllyCombatDamageTriggerEffect.boundToDealer(new CreateTokenCopyOfSourceEffect(true, 1))",
        "CreateTokenCopyOfEnteringPermanentEffect.nonlegendary()")],
        [f"{M}.effect.CreateTokenCopyOfEnteringPermanentEffect"]),
    ("i", "IkraShidiqiTheUsurper"): ([(
        "AllyCombatDamageTriggerEffect.boundToDealer(new GainLifeEffect(new SourceToughness()))",
        "new GainLifeEffect(new TriggeringPermanentToughness())")],
        [f"{M}.amount.TriggeringPermanentToughness"]),
    ("g", "GossipsTalent"): ([(
        """AllyCombatDamageTriggerEffect.boundToDealer(new MayEffect(
                        new FlickerEffect(
                                FlickerScope.SELF,
                                null,
                                ReturnTiming.IMMEDIATE,
                                TurnStep.END_STEP,
                                false,
                                null,
                                null,
                                0,
                                false,
                                false),
                        "Exile it, then return it to the battlefield under its owner's control?")));""",
        """new MayEffect(FlickerEffect.flickerTriggeringPermanent(),
                        "Exile it, then return it to the battlefield under its owner's control?"));""")], []),
    ("e", "ExperimentalLabStaffRoom"): ([(
        """                                AllyCombatDamageTriggerEffect.boundToDealer(new ChooseOneEffect(List.of(
                                                new ChooseOneEffect.ChooseOneOption(
                                                        "Turn that creature face up", new TurnSourceFaceUpEffect()),
                                                new ChooseOneEffect.ChooseOneOption(
                                                        "Put a +1/+1 counter on it",
                                                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))
                                        )))),""",
        """                                new ChooseOneEffect(List.of(
                                        new ChooseOneEffect.ChooseOneOption("Turn that creature face up",
                                                TurnSourceFaceUpEffect.ofTriggeringPermanent()),
                                        new ChooseOneEffect.ChooseOneOption("Put a +1/+1 counter on it",
                                                new PutCounterOnReferencedPermanentEffect(
                                                        PermanentReference.TRIGGERING,
                                                        CounterType.PLUS_ONE_PLUS_ONE))))),""")], REF_IMPORTS),
    ("a", "AragornHornburgHero"): ([(
        "AllyCombatDamageTriggerEffect.boundToDealer(new DoublePlusOneCountersOnSourceEffect())",
        "DoublePlusOneCountersOnSourceEffect.ofTriggeringPermanent()")], []),
    ("m", "MindbladeRender"): ([(
        """        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.WARRIOR)),
                TriggerOptions.ONE_OR_MORE,
                AllyCombatDamageTriggerEffect.opponentsDealtDamage(SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))));""",
        """        // "Whenever your opponents are dealt combat damage, if any of that damage was dealt by a Warrior": any
        // player's Warrior dealing combat damage to an opponent, once for the combat damage step.
        addTrigger(Triggers.creatureDealsCombatDamageToOpponent()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.WARRIOR)),
                TriggerOptions.ONE_OR_MORE,
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));""")], []),
    ("p", "PiratedCopy"): ([(
        """                        new TriggeredAbility(Triggers.creatureWithSameNameDealsCombatDamageToPlayer(),
                                new AllyCombatDamageTriggerEffect(
                                new PermanentHasSameNameAsSourcePredicate(),
                                new DrawCardEffect(1))))));""",
        """                        new TriggeredAbility(Triggers.creatureWithSameNameDealsCombatDamageToPlayer(),
                                new DrawCardEffect(1)))));""")], []),
}

IMPORT = re.compile(r"^import ([\w.]+)\.(\w+);\n", re.M)


def tidy_imports(text, added):
    """Adds {@code added} and drops the model imports whose simple name the class no longer uses, keeping order."""
    lines = text.splitlines(keepends=True)
    present = {f"{m.group(1)}.{m.group(2)}" for m in IMPORT.finditer(text)}
    for imp in added:
        if imp not in present:
            last = max(i for i, line in enumerate(lines) if line.startswith(f"import {M}."))
            lines.insert(last + 1, f"import {imp};\n")
            present.add(imp)
    text = "".join(lines)
    body = text[text.index("@CardRegistration"):]
    kept = []
    for line in text.splitlines(keepends=True):
        m = IMPORT.match(line)
        if m and m.group(1).startswith(M) and not re.search(rf"\b{m.group(2)}\b", body):
            continue
        kept.append(line)
    text = "".join(kept)
    # Model imports in alphabetical order, as the card classes keep them.
    block = [line for line in text.splitlines(keepends=True) if line.startswith(f"import {M}.")]
    start = text.index(block[0])
    end = text.index(block[-1]) + len(block[-1])
    middle = text[start:end]
    if all(line.startswith(f"import {M}.") for line in middle.splitlines(keepends=True)):
        text = text[:start] + "".join(sorted(block)) + text[end:]
    return text


def convert(letter, name, replacements, imports):
    path = CARDS / letter / PKG / letter / f"{name}.java"
    text = path.read_text()
    if "AllyCombatDamageTriggerEffect" not in text:
        print(f"unchanged {path.relative_to(ROOT)}")
        return
    for old, new in replacements:
        if text.count(old) != 1:
            sys.exit(f"{path}: expected one occurrence of\n{old}")
        text = text.replace(old, new)
    if "AllyCombatDamageTriggerEffect" in text.split("@CardRegistration", 1)[1]:
        sys.exit(f"{path}: AllyCombatDamageTriggerEffect still used")
    path.write_text(tidy_imports(text, imports))
    print(f"converted {path.relative_to(ROOT)}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--module", help="letter module to convert (default: all this run touches)")
    args = parser.parse_args()
    for (letter, name), (replacements, imports) in CONVERSIONS.items():
        if args.module is None or args.module == letter:
            convert(letter, name, replacements, imports)


if __name__ == "__main__":
    main()
