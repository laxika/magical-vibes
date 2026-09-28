package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ParleyCreateTokensEffect;

@CardRegistration(set = "LCC", collectorNumber = "46")
@CardRegistration(set = "LCC", collectorNumber = "78")
public class StormFleetNegotiator extends Card {

    public StormFleetNegotiator() {
        addEffect(EffectSlot.ON_ATTACK,
                new ParleyCreateTokensEffect(CreateTokenEffect.ofMapToken(1)));
    }
}
