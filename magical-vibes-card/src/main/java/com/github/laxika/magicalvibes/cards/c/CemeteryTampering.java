package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayImprintedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "SNC", collectorNumber = "69")
public class CemeteryTampering extends Card {

    public CemeteryTampering() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ImprintFromTopCardsEffect(5, true));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                SequenceEffect.of(
                        new MillEffect(3, MillRecipient.CONTROLLER),
                        new ConditionalEffect(new GraveyardCardThreshold(20, null),
                                new PlayImprintedCardWithoutPayingManaCostEffect())),
                "Mill three cards?"));
    }
}
