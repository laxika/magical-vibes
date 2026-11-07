#!/usr/bin/env python3
"""Step 15, run 17h: the granted and emblem abilities still held as effects become abilities.

Emblems' "whenever you cast ..." abilities are `TriggeredAbility`s on `CreateEmblemEffect` (Ajani, Sleeper Agent;
Dack Fayden; Jaya, Fiery Negotiator; Kaito, Cunning Infiltrator; Rowan, Scholar of Sparks; Tyvar Kell), Gale's
specialized abilities and Passionate Archaeologist's granted ability are filtered cast triggers, and Olivia, Crimson
Bride's granted state trigger is a granted `TriggeredAbility`. Idempotent: a card already converted is left alone.

Usage: step15-run17h-cards.py <letter module dir>...   e.g. magical-vibes-card/a magical-vibes-card/d
"""
import pathlib
import sys

CARDS_ROOT = "src/main/java/com/github/laxika/magicalvibes/cards"


def imports(source, wanted, unwanted=()):
    """Adds the `wanted` imports a file lacks and drops the `unwanted` ones it no longer uses."""
    for imp in unwanted:
        line = f"import {imp};\n"
        simple = imp.rsplit(".", 1)[1]
        if line in source and source.replace(line, "").count(simple) == 0:
            source = source.replace(line, "")
    anchor = source.index("\nimport ") + 1
    for imp in wanted:
        line = f"import {imp};\n"
        if line not in source:
            source = source[:anchor] + line + source[anchor:]
    return source


def replace_once(source, old, new, name):
    if new in source and old not in source:
        return source
    if source.count(old) != 1:
        raise SystemExit(f"{name}: expected one occurrence of:\n{old}")
    return source.replace(old, new)


M = "com.github.laxika.magicalvibes.model"

CONVERSIONS = {
    "t/TyvarKell.java": [(
        """                List.of(new CreateEmblemEffect(
                        List.of(new SpellCastTriggerEffect(
                                new CardSubtypePredicate(CardSubtype.ELF),
                                List.of(
                                        new GrantKeywordsToCastSpellEffect(Set.of(Keyword.HASTE)),
                                        new DrawCardEffect(2)
                                )
                        )),
                        EMBLEM_TEXT
                )),""",
        """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(
                                Triggers.youCastSpell().withFilter(new CardSubtypePredicate(CardSubtype.ELF)),
                                new GrantKeywordsToCastSpellEffect(Set.of(Keyword.HASTE)),
                                new DrawCardEffect(2)),
                        EMBLEM_TEXT
                )),""")],
    "r/RowanScholarOfSparks.java": [(
        """                List.of(new CreateEmblemEffect(
                        List.of(new SpellCastTriggerEffect(instantOrSorcery, List.of(
                                new MayPayManaEffect("{2}", new CopyTriggeringSpellEffect(), "Pay {2} to copy that spell?")))),
                        EMBLEM_TEXT)),""",
        """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.youCastSpell().withFilter(instantOrSorcery),
                                new MayPayManaEffect("{2}", new CopyTriggeringSpellEffect(), "Pay {2} to copy that spell?")),
                        EMBLEM_TEXT)),""")],
    "k/KaitoCunningInfiltrator.java": [(
        """                List.of(new CreateEmblemEffect(
                        List.of(SpellCastTriggerEffect.anyPlayer(null, List.of(new CreateTokenEffect(
                                "Ninja", 2, 1, CardColor.BLUE,
                                List.of(CardSubtype.NINJA), Set.of(), Set.of()
                        )))),""",
        """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.playerCastsSpell().anySpell(), new CreateTokenEffect(
                                "Ninja", 2, 1, CardColor.BLUE,
                                List.of(CardSubtype.NINJA), Set.of(), Set.of()
                        )),""")],
    "j/JayaFieryNegotiator.java": [(
        """                List.of(new CreateEmblemEffect(
                        List.of(new SpellCastTriggerEffect(redInstantOrSorcery,
                                List.of(new CopyTriggeringSpellEffect().withCopies(2)))),""",
        """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.youCastSpell().withFilter(redInstantOrSorcery),
                                new CopyTriggeringSpellEffect().withCopies(2)),""")],
    "d/DackFayden.java": [(
        """                List.of(new CreateEmblemEffect(
                        List.of(new SpellCastTriggerEffect(
                                null,
                                List.of(new GainControlOfTargetEffect(ControlDuration.PERMANENT)),
                                new StackEntryTargetsPermanentPredicate(new PermanentTruePredicate())
                        )),""",
        """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(
                                Triggers.youCastSpell().withStackEntryFilter(
                                        new StackEntryTargetsPermanentPredicate(new PermanentTruePredicate())),
                                new GainControlOfAllPermanentsMatchingEffect(
                                        new PermanentIsTargetOfTriggeringSpellPredicate())),""")],
    "a/AjaniSleeperAgent.java": [(
        """                List.of(new CreateEmblemEffect(
                        List.of(new SpellCastTriggerEffect(
                                creatureOrPlaneswalker,
                                List.of(new GivePoisonCountersEffect(2, PoisonRecipient.TARGET_PLAYER)),
                                null,
                                new com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter(
                                        new com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate(
                                                com.github.laxika.magicalvibes.model.filter.PlayerRelation.OPPONENT),
                                        "Target must be an opponent")
                        )),""",
        """                List.of(new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.youCastSpell().withFilter(creatureOrPlaneswalker),
                                new GivePoisonCountersEffect(2, PoisonRecipient.TARGET_PLAYER))
                                .withTargetFilter(new PlayerPredicateTargetFilter(
                                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                        "Target must be an opponent")),""")],
    "g/GaleConduitOfTheArcane.java": [(
        """    public static CardEffect specializedSpellCastTrigger(CardColor color) {
        CardTypePredicate instant = new CardTypePredicate(CardType.INSTANT);
        CardTypePredicate sorcery = new CardTypePredicate(CardType.SORCERY);
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(instant, sorcery));
        return switch (color) {
            case WHITE -> new SpellCastTriggerEffect(instantOrSorcery,
                    List.of(new CreateTokenEffect("Pegasus", 1, 1, CardColor.WHITE,
                            List.of(CardSubtype.PEGASUS), Set.of(Keyword.FLYING), Set.of())));
            case BLUE -> new SpellCastTriggerEffect(instantOrSorcery,
                    List.of(new DrawCardEffect(), new DiscardEffect(1, DiscardRecipient.CONTROLLER)));
            case BLACK -> new SpellCastTriggerEffect(instantOrSorcery,
                    List.of(new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT)));
            case RED -> new SpellCastTriggerEffect(instantOrSorcery,
                    List.of(new PerpetuallyGrantStaticEffectToSourceEffect(
                            new StaticBoostEffect(1, 0, GrantScope.ALL_OWN_CREATURES))));
            case GREEN -> new SpellCastTriggerEffect(instantOrSorcery,
                    List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
                    null, TargetFilters.creature());
        };
    }""",
        """    /**
     * Gives {@code card}, Gale specialized into {@code color}, its "whenever you cast an instant or sorcery spell"
     * ability.
     */
    public static void addSpecializedSpellCastTrigger(Card card, CardColor color) {
        SpellTrigger instantOrSorcery = Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY))));
        switch (color) {
            case WHITE -> card.addTrigger(instantOrSorcery,
                    new CreateTokenEffect("Pegasus", 1, 1, CardColor.WHITE,
                            List.of(CardSubtype.PEGASUS), Set.of(Keyword.FLYING), Set.of()));
            case BLUE -> card.addTrigger(instantOrSorcery,
                    new DrawCardEffect(), new DiscardEffect(1, DiscardRecipient.CONTROLLER));
            case BLACK -> card.addTrigger(instantOrSorcery,
                    new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT));
            case RED -> card.addTrigger(instantOrSorcery,
                    new PerpetuallyGrantStaticEffectToSourceEffect(
                            new StaticBoostEffect(1, 0, GrantScope.ALL_OWN_CREATURES)));
            case GREEN -> card.target(TargetFilters.creature()).addTrigger(instantOrSorcery,
                    new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2));
            default -> throw new IllegalArgumentException("Gale doesn't specialize into " + color);
        }
    }""")],
    "p/PassionateArchaeologist.java": [(
        """                new TriggeredAbility(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(
                        null,
                        List.of(new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TARGET_PLAYER)),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent"),
                        new StackEntryCastFromZonePredicate(Zone.EXILE))),""",
        """                new TriggeredAbility(
                        Triggers.youCastSpell().withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.EXILE)),
                        new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TARGET_PLAYER))
                        .withTargetFilter(new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent")),""")],
    "o/OliviaCrimsonBride.java": [(
        """                                new GrantEffectEffect(
                                        new StateTriggerEffect(
                                                new PermanentNotPredicate(
                                                        new PermanentControllerControlsPermanentPredicate(
                                                                new PermanentAllOfPredicate(List.of(
                                                                        new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE),
                                                                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))))),
                                                List.of(new ExileSelfEffect()),
                                                "When you don't control a legendary Vampire, exile this creature."),
                                        GrantScope.TARGET)))""",
        """                                new GrantEffectEffect(
                                        new GrantTriggeredAbilityEffect(new TriggeredAbility(
                                                Triggers.sourceState(new PermanentNotPredicate(
                                                        new PermanentControllerControlsPermanentPredicate(
                                                                new PermanentAllOfPredicate(List.of(
                                                                        new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE),
                                                                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)))))),
                                                new ExileSelfEffect()), GrantScope.TARGET),
                                        GrantScope.TARGET)))""")],
}

IMPORTS = {
    "t/TyvarKell.java": ([f"{M}.TriggeredAbility", f"{M}.trigger.Triggers"], [f"{M}.effect.SpellCastTriggerEffect"]),
    "r/RowanScholarOfSparks.java": ([f"{M}.TriggeredAbility", f"{M}.trigger.Triggers"],
                                    [f"{M}.effect.SpellCastTriggerEffect"]),
    "k/KaitoCunningInfiltrator.java": ([f"{M}.TriggeredAbility", f"{M}.trigger.Triggers"],
                                       [f"{M}.effect.SpellCastTriggerEffect"]),
    "j/JayaFieryNegotiator.java": ([f"{M}.TriggeredAbility", f"{M}.trigger.Triggers"],
                                   [f"{M}.effect.SpellCastTriggerEffect"]),
    "d/DackFayden.java": ([f"{M}.TriggeredAbility", f"{M}.trigger.Triggers",
                           f"{M}.effect.GainControlOfAllPermanentsMatchingEffect",
                           f"{M}.filter.PermanentIsTargetOfTriggeringSpellPredicate"],
                          [f"{M}.effect.SpellCastTriggerEffect"]),
    "a/AjaniSleeperAgent.java": ([f"{M}.TriggeredAbility", f"{M}.trigger.Triggers",
                                  f"{M}.filter.PlayerPredicateTargetFilter", f"{M}.filter.PlayerRelationPredicate",
                                  f"{M}.filter.PlayerRelation"],
                                 [f"{M}.effect.SpellCastTriggerEffect"]),
    "g/GaleConduitOfTheArcane.java": ([f"{M}.trigger.SpellTrigger"], [f"{M}.effect.SpellCastTriggerEffect"]),
    "p/PassionateArchaeologist.java": ([], [f"{M}.effect.SpellCastTriggerEffect"]),
    "o/OliviaCrimsonBride.java": ([f"{M}.TriggeredAbility", f"{M}.trigger.Triggers",
                                   f"{M}.effect.GrantTriggeredAbilityEffect"],
                                  [f"{M}.effect.StateTriggerEffect"]),
}


def main(modules):
    converted = 0
    for module in modules:
        letter = pathlib.Path(module).name
        for relative, edits in CONVERSIONS.items():
            if not relative.startswith(letter + "/"):
                continue
            path = pathlib.Path(module) / CARDS_ROOT / relative
            source = path.read_text()
            original = source
            for old, new in edits:
                source = replace_once(source, old, new, relative)
            wanted, unwanted = IMPORTS[relative]
            source = imports(source, wanted, unwanted)
            if source != original:
                path.write_text(source)
                converted += 1
                print(f"converted {relative}")
    print(f"{converted} card classes converted")


if __name__ == "__main__":
    main(sys.argv[1:])
