package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryAndConjureDuplicatesInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeSarevokEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

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
        target(TargetFilters.creatureYouControl()).addEffect(
                EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
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
                        new SpecializeSarevokEffect(color)),
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
                    new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET), boost);
            case BLUE, BLACK -> boost;
            case RED -> SequenceEffect.of(
                    new GrantKeywordEffect(Keyword.MENACE, GrantScope.TARGET), boost);
            case GREEN -> SequenceEffect.of(
                    new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET), boost);
        };
    }

    public static CardEffect specializationTrigger() {
        return new SeekLibraryAndConjureDuplicatesInGraveyardEffect(CREATURE_CARDS);
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
}
