#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 17b: "copy that spell" reads the trigger event, and no "you may" is unwrapped.

Idempotent; run once per letter module:

    for m in magical-vibes-card/*/; do python3 scripts/step15-copy-cast-spell-cards.py "$m"; done

Each card below wrote its "whenever you cast a [quality] spell, copy it" ability as CopyControllerCastSpellOnSpellCastEffect,
a trigger descriptor a class-keyed collector handler judged (the spell filter, the zone, "your first … each turn", the
Adventure, the intervening "if") and rebuilt into a copy effect with the spell bound in, reached through an unwrapped
"you may" on 14 of them. Each is now the cast trigger's filter and options, and CopyTriggeringSpellEffect, which copies
the stack entry's triggering spell, with the copy's modifications; the costs and the "you may" are the existing
MayEffect, MayPayManaEffect, MayPayTapPermanentsEffect and SacrificePermanentThenEffect. The emblems (Jaya, Rowan)
are SpellCastTriggerEffect emblem triggers.

Skyfire Kirin's "target creature with that spell's mana value" is a target filter on the spell's mana value as the
ability's X (PermanentManaValueEqualsXPredicate), and "gain control" the existing GainControlOfTargetEffect.

Each replacement replaces the whole chained expression (and its ';') that starts at an occurrence of the anchor and
contains the old class; the imports the new text needs are added and the unused ones removed afterwards with
scripts/strip-unused-imports-in.py.
"""
import pathlib
import re
import subprocess
import sys

OLD = ("CopyControllerCastSpellOnSpellCastEffect", "GainControlOfTargetCreatureByCastSpellManaValueEffect")

IS = ("new CardAnyOfPredicate(List.of(\n"
      "                        new CardTypePredicate(CardType.INSTANT),\n"
      "                        new CardTypePredicate(CardType.SORCERY)))")

ADD = "addTrigger(Triggers.youCastSpell()"
COPY = "new CopyTriggeringSpellEffect()"

CARDS = {
    "AlaniaDivergentStorm.java": ("target(", """target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.youCastSpell().withStackEntryFilter(
                        new StackEntryIsFirstSpellOfKindThisTurnPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY),
                                new CardAllOfPredicate(List.of(
                                        new CardSubtypePredicate(CardSubtype.OTTER),
                                        new CardNotPredicate(new CardIsSelfPredicate())))))),
                new MayEffect(
                        SequenceEffect.of(new DrawCardForTargetPlayerEffect(1, false, true), """ + COPY + """),
                        "Have target opponent draw a card?"));"""),
    "ArchmageOfEchoes.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(qualifyingPermanentSpell),
                """ + COPY + """.withoutNewTargetChoice().permanentSpellCopiesAreTokens());"""),
    "AzizaMageTowerCaptain.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(""" + IS + """),
                new MayPayTapPermanentsEffect(
                        new TapMultiplePermanentsCost(3, new PermanentIsCreaturePredicate()),
                        """ + COPY + """,
                        "Tap three untapped creatures you control to copy that spell?"));"""),
    "CaseOfTheShiftingVisage.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(nonlegendaryCreatureSpell),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsSolved()),
                new CopyTriggeringSpellEffect(true).withoutNewTargetChoice());"""),
    "ChancellorOfTales.java": (ADD, """addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryCastWithAdventurePredicate()),
                new MayEffect(""" + COPY + """, "Copy that spell?"));"""),
    "ChandraHopesBeacon.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(""" + IS + """),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                """ + COPY + """);"""),
    "ClovenCasting.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(new CardAllOfPredicate(List.of(
                        new CardIsMulticoloredPredicate(),
                        """ + IS + """))),
                new MayPayManaEffect("{1}", """ + COPY + """, "Pay {1} to copy that spell?"));"""),
    "CosmicCrucible.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN),
                new MayEffect(""" + COPY + """.permanentSpellCopiesAreTokens(), "Copy that spell?"));"""),
    "DonalHeraldOfWings.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(qualifyingCreature),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new MayEffect(
                        """ + COPY + """.permanentSpellCopiesAreTokens()
                                .asAdditionally(List.of(CardSubtype.SPIRIT), 1, 1),
                        "Copy that creature spell?"));"""),
    "DoubleDown.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.ASSASSIN),
                        new CardSubtypePredicate(CardSubtype.MERCENARY),
                        new CardSubtypePredicate(CardSubtype.PIRATE),
                        new CardSubtypePredicate(CardSubtype.ROGUE),
                        new CardSubtypePredicate(CardSubtype.WARLOCK)))),
                new CopyTriggeringSpellEffect(true).withoutNewTargetChoice());"""),
    "DoubleVision.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(""" + IS + """).nthThisTurn(1),
                """ + COPY + """);"""),
    "FireLordAzula.java": (ADD, """addTrigger(Triggers.youCastSpell().anySpell(),
                TriggerOptions.DEFAULT.withEventCondition(new SourceIsAttacking()),
                """ + COPY + """);"""),
    "GeistOfRegret.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery).castFrom(Zone.GRAVEYARD),
                """ + COPY + """);"""),
    "GorionWiseMentor.java": (ADD, """addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryCastWithAdventurePredicate()),
                new MayEffect(""" + COPY + """, "Copy that spell?"));"""),
    "InvocationOfTheFounders.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(""" + IS + """).castFrom(Zone.HAND),
                new MayEffect(""" + COPY + """, "Copy that spell?"));"""),
    "IronManBleedingEdge.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.ARTIFACT)),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN),
                new MayEffect(
                        """ + COPY + """.permanentSpellCopiesAreTokens()
                                .withoutSupertypes(Set.of(CardSupertype.LEGENDARY)),
                        "Copy that spell?"));"""),
    "JaceReawakened.java": ("new TriggeredAbility(", """new TriggeredAbility(Triggers.youCastSpell().anySpell(),
                        """ + COPY + """)"""),
    "JayaFieryNegotiator.java": ("List.of(new CopyControllerCastSpellOnSpellCastEffect(", """List.of(new SpellCastTriggerEffect(redInstantOrSorcery,
                                List.of(""" + COPY + """.withCopies(2))))"""),
    "KalamaxTheStormsire.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(instant).nthThisTurn(1),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped()),
                """ + COPY + """);"""),
    "LeylineOfResonance.java": (ADD, """addTrigger(Triggers.youCastSpell()
                        .withFilter(""" + IS + """)
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                new StackEntryTargetsOnlySingleCreaturePredicate(),
                                new StackEntryTargetsPermanentPredicate(
                                        new PermanentControlledBySourceControllerPredicate())))),
                """ + COPY + """);"""),
    "LuckyClover.java": (ADD, """addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryCastWithAdventurePredicate()),
                """ + COPY + """);"""),
    "MelekIzzetParagon.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(""" + IS + """).castFrom(Zone.LIBRARY),
                """ + COPY + """);"""),
    "MendicantCoreGuidelight.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.ARTIFACT)),
                TriggerOptions.DEFAULT.withInterveningIf(new MaxSpeed()),
                new MayPayManaEffect("{1}", new CopyTriggeringSpellEffect(true).withoutNewTargetChoice(),
                        "Pay {1} to copy that spell?"));"""),
    "MicaReaderOfRuins.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(""" + IS + """),
                new MayEffect(
                        new SacrificePermanentThenEffect(new PermanentIsArtifactPredicate(), """ + COPY + """,
                                "an artifact", false, false),
                        "Sacrifice an artifact to copy that spell?"));"""),
    "Mirari.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(""" + IS + """),
                new MayPayManaEffect("{3}", """ + COPY + """, "Pay {3} to copy that spell?"));"""),
    "MyWingsEnfoldAll.java": ("new TriggeredAbility(", """new TriggeredAbility(Triggers.youCastSpell().withFilter(instantOrSorcery),
                                """ + COPY + """)"""),
    "Nalfeshnee.java": (ADD, """addTrigger(Triggers.youCastSpell().anySpell().castFrom(Zone.EXILE),
                """ + COPY + """.permanentSpellCopiesAreTokens().permanentSpellCopiesGainHaste()
                        .sacrificedAtEndStep());"""),
    "OwlinSpiralmancer.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(xSpell).nthThisTurn(1),
                new MayEffect(new CopyTriggeringSpellEffect(true), "Copy that spell?"));"""),
    "ProvidenceOfNight.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(new CardHasHybridManaPredicate()),
                """ + COPY + """);"""),
    "PyromancerAscension.java": ("addTrigger(Triggers.youCastSpell(),", """addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery),
                TriggerOptions.DEFAULT.withEventCondition(new SourceCounterThreshold(2, CounterType.QUEST)),
                new MayEffect(""" + COPY + """, "Copy that spell?"));"""),
    "RealityIsMineToControl.java": (ADD, """addTrigger(Triggers.youCastSpell().anySpell(), new MayEffect(
                new SacrificePermanentThenEffect(new PermanentIsSourcePermanentPredicate(),
                        """ + COPY + """.permanentSpellCopiesAreTokens(), "this scheme", false, false),
                "Abandon this scheme and copy that spell?"));"""),
    "ReflectionsOfLittjara.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(new CardHasSourceChosenSubtypePredicate(false)),
                new CopyTriggeringSpellEffect(true).withoutNewTargetChoice());"""),
    "ReflectiveGolem.java": (ADD, """addTrigger(Triggers.youCastSpell()
                        .withFilter(instantOrSorcery)
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                new StackEntryIsSingleTargetPredicate(),
                                new StackEntryTargetsSourcePredicate()))),
                new MayPayManaEffect("{2}", """ + COPY + """, "Pay {2} to copy that spell?"));"""),
    "RikuOfTwoReflections.java": ("addTrigger(Triggers.youCastSpell(),", """addTrigger(Triggers.youCastSpell().withFilter(""" + IS + """),
                new MayPayManaEffect("{U}{R}", """ + COPY + """, "Pay {U}{R} to copy that spell?"));"""),
    "RowanScholarOfSparks.java": ("List.of(new CopyControllerCastSpellOnSpellCastEffect(", """List.of(new SpellCastTriggerEffect(instantOrSorcery, List.of(
                                new MayPayManaEffect("{2}", """ + COPY + """, "Pay {2} to copy that spell?"))))"""),
    "JinGitaxiasProgressTyrant.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(artifactInstantOrSorcery),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                """ + COPY + """);"""),
    "SevinneTheChronoclasm.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(""" + IS + """)
                        .castFrom(Zone.GRAVEYARD).nthThisTurn(1),
                """ + COPY + """);"""),
    "SpiderVerse.java": (ADD, """addTrigger(Triggers.youCastSpell().anySpell().notCastFrom(Zone.HAND),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN),
                new MayEffect(""" + COPY + """.permanentSpellCopiesGainHaste(), "Copy that spell?"));"""),
    "SpinerockTyrant.java": (ADD, """addTrigger(Triggers.youCastSpell()
                        .withFilter(""" + IS + """)
                        .withStackEntryFilter(new StackEntryIsSingleTargetPredicate()),
                new MayEffect(""" + COPY + """.withGrantedKeywords(Set.of(Keyword.WITHER)), "Copy that spell?"));"""),
    "SwarmIntelligence.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(""" + IS + """),
                new MayEffect(""" + COPY + """, "Copy that spell?"));"""),
    "TawnosTheToymaker.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(beastOrBirdCreature),
                new MayEffect(new CopyTriggeringSpellEffect(true).withAdditionalTypes(Set.of(CardType.ARTIFACT)),
                        "Copy that creature spell?"));"""),
    "TheSixthDoctor.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(new CardIsHistoricPredicate()),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new CopyTriggeringSpellEffect(true).withoutNewTargetChoice()
                        .withoutSupertypes(Set.of(CardSupertype.LEGENDARY)));"""),
    "UnboundFlourishing.java": ("addTrigger(Triggers.youCastSpell(),", """addTrigger(Triggers.youCastSpell().withFilter(new CardAllOfPredicate(List.of(
                        new CardHasXInManaCostPredicate(),
                        """ + IS + """))),
                """ + COPY + """);"""),
    "VoloGuideToMonsters.java": (ADD, """addTrigger(Triggers.youCastSpell().withFilter(qualifyingCreatureSpell),
                new CopyTriggeringSpellEffect(true).withoutNewTargetChoice());"""),
    "SkyfireKirin.java": ("target(", """target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentManaValueEqualsXPredicate())),
                "Target must be a creature with that spell's mana value"))
                .addTrigger(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.SPIRIT),
                                new CardSubtypePredicate(CardSubtype.ARCANE)))),
                        new MayEffect(new GainControlOfTargetEffect(ControlDuration.END_OF_TURN),
                                "Gain control of target creature with that spell's mana value until end of turn?"));"""),
}

IMPORTS = {
    "CopyTriggeringSpellEffect": "com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect",
    "SpellCastTriggerEffect": "com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect",
    "MayEffect": "com.github.laxika.magicalvibes.model.effect.MayEffect",
    "MayPayManaEffect": "com.github.laxika.magicalvibes.model.effect.MayPayManaEffect",
    "MayPayTapPermanentsEffect": "com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect",
    "TapMultiplePermanentsCost": "com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost",
    "SacrificePermanentThenEffect": "com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect",
    "SequenceEffect": "com.github.laxika.magicalvibes.model.effect.SequenceEffect",
    "GainControlOfTargetEffect": "com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect",
    "ControlDuration": "com.github.laxika.magicalvibes.model.effect.ControlDuration",
    "TriggerOptions": "com.github.laxika.magicalvibes.model.trigger.TriggerOptions",
    "TriggerLimit": "com.github.laxika.magicalvibes.model.trigger.TriggerLimit",
    "CardAnyOfPredicate": "com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate",
    "CardAllOfPredicate": "com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate",
    "CardTypePredicate": "com.github.laxika.magicalvibes.model.filter.CardTypePredicate",
    "CardNotPredicate": "com.github.laxika.magicalvibes.model.filter.CardNotPredicate",
    "StackEntryCastWithAdventurePredicate": "com.github.laxika.magicalvibes.model.filter.StackEntryCastWithAdventurePredicate",
    "StackEntryIsFirstSpellOfKindThisTurnPredicate":
        "com.github.laxika.magicalvibes.model.filter.StackEntryIsFirstSpellOfKindThisTurnPredicate",
    "PermanentPredicateTargetFilter": "com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter",
    "PermanentAllOfPredicate": "com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate",
    "PermanentIsCreaturePredicate": "com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate",
    "PermanentManaValueEqualsXPredicate": "com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate",
    "CardType": "com.github.laxika.magicalvibes.model.CardType",
    "CardSupertype": "com.github.laxika.magicalvibes.model.CardSupertype",
    "CardSubtype": "com.github.laxika.magicalvibes.model.CardSubtype",
    "Keyword": "com.github.laxika.magicalvibes.model.Keyword",
    "Zone": "com.github.laxika.magicalvibes.model.Zone",
    "List": "java.util.List",
    "Set": "java.util.Set",
}


def expression_end(src, start):
    """The end of the chained call expression starting at start (through a trailing ';')."""
    i = src.index("(", start)
    while True:
        depth = 0
        while True:
            c = src[i]
            if c == '"':
                i = src.index('"', i + 1)
            elif c == "(":
                depth += 1
            elif c == ")":
                depth -= 1
                if depth == 0:
                    break
            i += 1
        i += 1
        m = re.match(r"\s*\.\s*[A-Za-z]+\s*\(", src[i:])
        if not m:
            break
        i = i + m.end() - 1
    if src[i:i + 1] == ";":
        i += 1
    return i


def add_imports(src, text):
    imports = set(re.findall(r"^import ([\w.]+);$", src, re.M))
    needed = [fqn for name, fqn in IMPORTS.items()
              if re.search(r"\b" + name + r"\b", text) and fqn not in imports]
    if not needed:
        return src
    last = list(re.finditer(r"^import [\w.]+;\n", src, re.M))[-1]
    lines = "".join(f"import {fqn};\n" for fqn in sorted(needed))
    return src[:last.end()] + lines + src[last.end():]


def convert(path, anchor, new):
    src = path.read_text()
    body = src[src.index("{", src.index(" class ")):]
    if not any(old in body for old in OLD):
        return False
    start = 0
    while True:
        start = src.index(anchor, start)
        end = expression_end(src, start)
        if any(old in src[start:end] for old in OLD):
            break
        start = end
    converted = src[:start] + new + src[end:]
    converted = re.sub(r"^import [\w.]+\.(" + "|".join(OLD) + r");\n", "", converted, flags=re.M)
    converted = add_imports(converted, new)
    path.write_text(converted)
    print(f"converted {path}")
    return True


def main():
    module = pathlib.Path(sys.argv[1])
    changed = []
    for path in sorted(module.glob("src/main/java/**/*.java")):
        if path.name in CARDS and convert(path, *CARDS[path.name]):
            changed.append(str(path))
    if changed:
        subprocess.run([sys.executable, str(pathlib.Path(__file__).with_name("strip-unused-imports-in.py")),
                        *changed], check=True)


if __name__ == "__main__":
    main()
