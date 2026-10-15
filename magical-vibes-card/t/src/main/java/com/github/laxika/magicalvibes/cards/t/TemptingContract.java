package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferCreateTokensEffect;

@CardRegistration(set = "C21", collectorNumber = "78")
public class TemptingContract extends Card {

    public TemptingContract() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new TemptingOfferCreateTokensEffect(CreateTokenEffect.ofTreasureToken(1), false));
    }
}
