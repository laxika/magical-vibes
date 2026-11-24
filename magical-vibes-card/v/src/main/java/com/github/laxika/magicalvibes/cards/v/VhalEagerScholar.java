package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostEventValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "8")
public class VhalEagerScholar extends Card {

    public VhalEagerScholar() {
        addLootAbility(this);

        addSpecializeAbility(CardColor.WHITE, CardSubtype.PLAINS);
        addSpecializeAbility(CardColor.BLUE, CardSubtype.ISLAND);
        addSpecializeAbility(CardColor.BLACK, CardSubtype.SWAMP);
        addSpecializeAbility(CardColor.RED, CardSubtype.MOUNTAIN);
        addSpecializeAbility(CardColor.GREEN, CardSubtype.FOREST);
    }

    public static void addLootAbility(Card card) {
        card.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DrawCardEffect(), new DiscardEffect(1, Subjects.you()),
                        new PutCountersOnSelfEffect(CounterType.STUDY)),
                "{T}: Draw a card, then discard a card. Put a study counter on Vhal, Eager Scholar."));
    }

    public static void addSpecializedLootAbility(Card card) {
        card.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DrawCardEffect(), new DiscardEffect(1, Subjects.you())),
                "{T}: Draw a card, then discard a card."));
    }

    private void addSpecializeAbility(CardColor color, CardSubtype basicLandType) {
        CardPredicate discardFilter = new CardAnyOfPredicate(List.of(
                new CardColorPredicate(color), new CardSubtypePredicate(basicLandType)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new DiscardCardTypeCost(discardFilter,
                                color.name().toLowerCase() + " card or " + basicLandType.name().toLowerCase()),
                        new SpecializeEffect(color, VhalEagerScholar::specializedFace)),
                "Specialize {5} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    /**
     * What the face does with the study counters its "when this creature specializes" ability removed: the white, black
     * and red faces' "when you do" ability, whose "that many" is its event value, and the blue and green faces' next
     * instruction of the same ability, reading the removal step.
     */
    public static CardEffect specializedTrigger(CardColor color) {
        AmountThisWay removed = new AmountThisWay(StepKind.REMOVE_COUNTERS);
        return switch (color) {
            case WHITE -> DistributeCountersAmongTargetsEffect.chosenAmongAnyNumberOfTargetCreatures(
                    CounterType.PLUS_ONE_PLUS_ONE, new EventValue(), null);
            // "Look at that many cards from the top of your library."
            case BLUE -> LookAtTopCardsEffect.chooseNToHandRestOnBottomRandom(removed, 1);
            case BLACK -> new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                    new CardTypePredicate(CardType.CREATURE), 1, false, false,
                    null, 0, new EventValue(), null, null, null, 0,
                    com.github.laxika.magicalvibes.model.GraveyardSearchScope.ALL_GRAVEYARDS,
                    false, false, false, 1);
            case RED -> new DealDamageToTargetCreatureOrPlaneswalkerEffect(
                    new EventValue(), creatureOrPlaneswalkerAnOpponentControls());
            // "Seek two creature cards with mana value less than or equal to the number of study counters removed this
            // way. Put one of them onto the battlefield and shuffle the other into your library."
            case GREEN -> SequenceEffect.of(
                    new SeekLibraryEffect(new Fixed(2), new CardTypePredicate(CardType.CREATURE),
                            LibrarySearchDestination.HAND, new ManaValueBound(removed, false, 0)),
                    ChooseCardEffect.oneOf(Subjects.cardsThisWay(StepKind.SEEK),
                            "a sought creature card to put onto the battlefield"),
                    new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE)),
                    PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.except(
                            Subjects.cardsThisWay(StepKind.SEEK), Subjects.cardsThisWay(StepKind.CHOOSE))),
                    new ShuffleLibraryEffect(false));
        };
    }

    public static PermanentPredicate creatureOrPlaneswalkerAnOpponentControls() {
        return new PermanentAllOfPredicate(List.of(
                new com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(), new PermanentIsPlaneswalkerPredicate())),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }

    public static void setSpecializedBaseCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("8");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.WIZARD));
        card.setColor(CardColor.BLUE);
        card.setColors(List.of(CardColor.BLUE));
        card.setColorIdentity(List.of(CardColor.BLUE));
        card.setKeywords(Set.of());
    }

    /**
     * Writes Vhal's specialized face for {@code color}; returns its "when this creature specializes" ability: "remove all
     * study counters from it", then the face's next instruction, which for the white, black and red faces is a "when you
     * do" ability (CR 603.12) made only if counters were removed, its "that many" the number removed.
     */
    public static List<CardEffect> specializedFace(Card face, CardColor color) {
        setSpecializedBaseCharacteristics(face);
        addSpecializedLootAbility(face);
        face.setPower(4);
        face.setToughness(4);
        setFaceCharacteristics(face, color);
        CardEffect next = specializedTrigger(color);
        installSpecializedTargetGroup(face, color);
        if (!face.getTargetGroups().isEmpty()) {
            face.registerEffectTargetIndex(next, 0);
        }
        CardEffect removeStudyCounters = new RemoveCounterFromSourceEffect(CounterType.STUDY, Integer.MAX_VALUE);
        return switch (color) {
            case BLUE, GREEN -> List.of(removeStudyCounters, next);
            default -> List.of(removeStudyCounters, new ConditionalEffect(HappenedThisWay.removedCounters(),
                    QueueReflexiveAbilityEffect.withEventValue(next, new AmountThisWay(StepKind.REMOVE_COUNTERS))));
        };
    }

    private static void installSpecializedTargetGroup(Card specialized, CardColor color) {
        switch (color) {
            case WHITE -> specialized.targetUpTo(new com.github.laxika.magicalvibes.model.amount.EventValue(),
                    TargetFilters.creature(), 100);
            case BLACK -> specialized.target(
                    new GraveyardCardPredicateTargetFilter(
                            new CardAllOfPredicate(List.of(
                                    new CardTypePredicate(CardType.CREATURE),
                                    new CardManaValueAtMostEventValuePredicate())),
                            GraveyardSearchScope.ALL_GRAVEYARDS));
            case RED -> specialized.target(new PermanentPredicateTargetFilter(
                    creatureOrPlaneswalkerAnOpponentControls(),
                    "Target must be a creature or planeswalker an opponent controls"));
            default -> {
            }
        }
    }

    private static void setFaceCharacteristics(Card card, CardColor color) {
        switch (color) {
            case WHITE -> {
                card.setName("Vhal, Scholar of Tactics");
                card.setManaCost("{2}{W}{U}");
                card.setColors(List.of(CardColor.WHITE, CardColor.BLUE));
                card.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLUE));
                card.setColor(CardColor.WHITE);
                card.setCardText("When this creature specializes, remove all study counters from it. "
                        + "When you do, distribute that many +1/+1 counters among any number of target creatures.\n"
                        + "{T}: Draw a card, then discard a card.");
            }
            case BLUE -> {
                card.setName("Vhal, Scholar of Prophecy");
                card.setManaCost("{2}{U}{U}");
                card.setCardText("When this creature specializes, remove all study counters from it. "
                        + "Look at that many cards from the top of your library. Put one of those cards into your hand "
                        + "and the rest on the bottom of your library in a random order.\n"
                        + "{T}: Draw a card, then discard a card.");
            }
            case BLACK -> {
                card.setName("Vhal, Scholar of Mortality");
                card.setManaCost("{2}{U}{B}");
                card.setColors(List.of(CardColor.BLUE, CardColor.BLACK));
                card.setColorIdentity(List.of(CardColor.BLUE, CardColor.BLACK));
                card.setColor(CardColor.BLACK);
                card.setCardText("When this creature specializes, remove all study counters from it. "
                        + "When you do, put target creature card with mana value less than or equal to the number "
                        + "of study counters removed this way from a graveyard onto the battlefield.\n"
                        + "{T}: Draw a card, then discard a card.");
            }
            case RED -> {
                card.setName("Vhal, Scholar of Elements");
                card.setManaCost("{2}{U}{R}");
                card.setColors(List.of(CardColor.BLUE, CardColor.RED));
                card.setColorIdentity(List.of(CardColor.BLUE, CardColor.RED));
                card.setColor(CardColor.RED);
                card.setCardText("When this creature specializes, remove all study counters from it. "
                        + "When you do, Vhal, Scholar of Elements deals that much damage to target creature "
                        + "or planeswalker an opponent controls.\n"
                        + "{T}: Draw a card, then discard a card.");
            }
            case GREEN -> {
                card.setName("Vhal, Scholar of Creation");
                card.setManaCost("{2}{G}{U}");
                card.setColors(List.of(CardColor.GREEN, CardColor.BLUE));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.BLUE));
                card.setColor(CardColor.GREEN);
                card.setCardText("When this creature specializes, remove all study counters from it. "
                        + "Seek two creature cards with mana value less than or equal to the number "
                        + "of study counters removed this way. Put one of them onto the battlefield and shuffle the "
                        + "other into your library.\n{T}: Draw a card, then discard a card.");
            }
            default -> throw new IllegalStateException("Unsupported Vhal specialization color: " + color);
        }
    }
}
