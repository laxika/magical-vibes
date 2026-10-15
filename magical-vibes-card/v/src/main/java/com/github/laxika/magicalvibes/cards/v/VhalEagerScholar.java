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
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SeekTwoCardsToBattlefieldWithinManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeVhalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

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
                List.of(new DrawCardEffect(), new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                        new PutCountersOnSelfEffect(CounterType.STUDY)),
                "{T}: Draw a card, then discard a card. Put a study counter on Vhal, Eager Scholar."));
    }

    private void addSpecializeAbility(CardColor color, CardSubtype basicLandType) {
        CardPredicate discardFilter = new CardAnyOfPredicate(List.of(
                new CardColorPredicate(color), new CardSubtypePredicate(basicLandType)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new DiscardCardTypeCost(discardFilter,
                                color.name().toLowerCase() + " card or " + basicLandType.name().toLowerCase()),
                        new SpecializeVhalEffect(color)),
                "Specialize {5} — " + color.name().toLowerCase()
                        + " (discard a " + color.name().toLowerCase() + " card or "
                        + basicLandType.name().toLowerCase() + ")",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    public static CardEffect specializedTrigger(CardColor color) {
        return switch (color) {
            case WHITE -> DistributeCountersAmongTargetsEffect.chosenAmongAnyNumberOfTargetCreatures(
                    CounterType.PLUS_ONE_PLUS_ONE, new EventValue(), null);
            case BLUE -> LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new EventValue());
            case BLACK -> new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                    new CardTypePredicate(CardType.CREATURE), 1, false, false,
                    null, 0, new EventValue(), null, null, null, 0,
                    com.github.laxika.magicalvibes.model.GraveyardSearchScope.ALL_GRAVEYARDS,
                    false, false, false, 1);
            case RED -> new DealDamageToTargetCreatureOrPlaneswalkerEffect(
                    new EventValue(), creatureOrPlaneswalkerAnOpponentControls());
            case GREEN -> new SeekTwoCardsToBattlefieldWithinManaValueEffect(
                    new CardTypePredicate(CardType.CREATURE), new EventValue());
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
}
