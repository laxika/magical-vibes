package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardTypeCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "MH2", collectorNumber = "136")
public class MountVelusManticore extends Card {

    public MountVelusManticore() {
        // At the beginning of combat on your turn, you may discard a card. When you do, this
        // creature deals X damage to any target, where X is the number of card types the discarded
        // card has.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new MayEffect(
                new DiscardCardThenEffect(null,
                        new DealDamageToAnyTargetEffect(new LastDiscardedCardTypeCount()),
                        "a card"),
                "Discard a card to deal damage to any target?"));
    }
}
