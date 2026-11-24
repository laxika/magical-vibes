package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "9")
public class SarevokTheUsurper extends Card {

    private static final CardPredicate CREATURE_CARDS = new CardTypePredicate(CardType.CREATURE);
    private static final CardPredicate CREATURE_INSTANT_OR_SORCERY_CARDS = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.CREATURE),
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));

    public SarevokTheUsurper() {
        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new BoostTargetCreatureEffect(
                        new CardsInGraveyard(CREATURE_CARDS, CountScope.CONTROLLER), new Fixed(0)));

        addSpecializeAbility(CardColor.WHITE, CardSubtype.PLAINS);
        addSpecializeAbility(CardColor.BLUE, CardSubtype.ISLAND);
        addSpecializeAbility(CardColor.BLACK, CardSubtype.SWAMP);
        addSpecializeAbility(CardColor.RED, CardSubtype.MOUNTAIN);
        addSpecializeAbility(CardColor.GREEN, CardSubtype.FOREST);
    }

    private void addSpecializeAbility(CardColor color, CardSubtype basicLandType) {
        CardPredicate discardFilter = new CardAnyOfPredicate(List.of(
                new CardColorPredicate(color), new CardSubtypePredicate(basicLandType)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new DiscardCardTypeCost(discardFilter,
                                color.name().toLowerCase() + " card or " + basicLandType.name().toLowerCase()),
                        new SpecializeEffect(color, SarevokTheUsurper::specializedFace)),
                "Specialize {3} \u2014 " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static CardEffect specializedCombatTrigger(CardColor color) {
        CardPredicate graveyardFilter = color == CardColor.BLUE
                ? CREATURE_INSTANT_OR_SORCERY_CARDS : CREATURE_CARDS;
        CardEffect boost = new BoostTargetCreatureEffect(
                new CardsInGraveyard(graveyardFilter, CountScope.CONTROLLER), new Fixed(0));
        return switch (color) {
            case WHITE -> SequenceEffect.of(
                    new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.target()), boost);
            case BLUE, BLACK -> boost;
            case RED -> SequenceEffect.of(
                    new GrantKeywordEffect(Keyword.MENACE, Subjects.target()), boost);
            case GREEN -> SequenceEffect.of(
                    new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target()), boost);
        };
    }

    public static CardEffect specializationTrigger() {
        // "Seek a creature card and put it into your graveyard, then conjure two duplicates of it into your graveyard."
        return SequenceEffect.of(
                new SeekLibraryEffect(1, CREATURE_CARDS, LibrarySearchDestination.GRAVEYARD),
                ConjureCardsEffect.duplicateOf(Subjects.cardsThisWay(StepKind.SEEK), ConjureDestination.intoGraveyard())
                        .times(2));
    }

    public static void setSpecializedBaseCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("9");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.KNIGHT));
        card.setColor(CardColor.BLACK);
        card.setColors(List.of(CardColor.BLACK));
        card.setColorIdentity(List.of(CardColor.BLACK));
        card.setKeywords(Set.of());
    }

    /** Writes Sarevok's specialized face for {@code color}; returns its "when this creature specializes" ability. */
    public static List<CardEffect> specializedFace(Card face, CardColor color) {
        setSpecializedBaseCharacteristics(face);
        face.setPower(4);
        face.setToughness(4);
        setFaceCharacteristics(face, color);

        face.target(TargetFilters.creatureYouControl()).addTrigger(
                Triggers.beginningOfCombat(ActivePlayer.YOU),
                specializedCombatTrigger(color));
        return color == CardColor.BLACK ? List.of(specializationTrigger()) : List.of();
    }

    private static void setFaceCharacteristics(Card card, CardColor color) {
        switch (color) {
            case WHITE -> {
                card.setName("Sarevok, Divine Usurper");
                card.setManaCost("{3}{W}{B}");
                card.setColors(List.of(CardColor.WHITE, CardColor.BLACK));
                card.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLACK));
                card.setColor(CardColor.WHITE);
                card.setKeywords(Set.of(Keyword.FIRST_STRIKE));
                card.setCardText("First strike\nAt the beginning of combat on your turn, target creature you control gains first strike and gets +X/+0 until end of turn, where X is the number of creature cards in your graveyard.");
            }
            case BLUE -> {
                card.setName("Sarevok, Deceitful Usurper");
                card.setManaCost("{3}{U}{B}");
                card.setColors(List.of(CardColor.BLUE, CardColor.BLACK));
                card.setColorIdentity(List.of(CardColor.BLUE, CardColor.BLACK));
                card.setColor(CardColor.BLUE);
                card.setCardText("At the beginning of combat on your turn, target creature you control gets +X/+0 until end of turn, where X is the number of creature, instant, and sorcery cards in your graveyard.");
            }
            case BLACK -> {
                card.setName("Sarevok, Deadly Usurper");
                card.setManaCost("{3}{B}{B}");
                card.setCardText("When this creature specializes, seek a creature card and put it into your graveyard, then conjure two duplicates of it into your graveyard.\nAt the beginning of combat on your turn, target creature you control gets +X/+0 until end of turn, where X is the number of creature cards in your graveyard.");
            }
            case RED -> {
                card.setName("Sarevok, Ferocious Usurper");
                card.setManaCost("{3}{B}{R}");
                card.setColors(List.of(CardColor.RED, CardColor.BLACK));
                card.setColorIdentity(List.of(CardColor.RED, CardColor.BLACK));
                card.setColor(CardColor.RED);
                card.setKeywords(Set.of(Keyword.MENACE));
                card.setCardText("Menace\nAt the beginning of combat on your turn, target creature you control gains menace and gets +X/+0 until end of turn, where X is the number of creature cards in your graveyard.");
            }
            case GREEN -> {
                card.setName("Sarevok, Mighty Usurper");
                card.setManaCost("{3}{B}{G}");
                card.setColors(List.of(CardColor.GREEN, CardColor.BLACK));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.BLACK));
                card.setColor(CardColor.GREEN);
                card.setKeywords(Set.of(Keyword.TRAMPLE));
                card.setCardText("Trample\nAt the beginning of combat on your turn, target creature you control gains trample and gets +X/+0 until end of turn, where X is the number of creature cards in your graveyard.");
            }
        }
    }
}
