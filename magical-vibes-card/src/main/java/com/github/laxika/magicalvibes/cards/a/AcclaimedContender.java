package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsOtherPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "1")
public class AcclaimedContender extends Card {

    public AcclaimedContender() {
        CardAnyOfPredicate knightAuraEquipmentOrLegendaryArtifact = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.KNIGHT),
                new CardIsAuraPredicate(),
                new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardSupertypePredicate(CardSupertype.LEGENDARY)))));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(
                        new ControlsOtherPermanentCount(1, new PermanentHasSubtypePredicate(CardSubtype.KNIGHT)),
                        LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                                5, knightAuraEquipmentOrLegendaryArtifact)));
    }
}
