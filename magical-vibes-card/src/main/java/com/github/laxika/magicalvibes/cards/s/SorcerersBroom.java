package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "ELD", collectorNumber = "232")
public class SorcerersBroom extends Card {

    public SorcerersBroom() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, new MayPayManaEffect(
                "{3}",
                new CreateTokenCopyOfSourceEffect(),
                "Pay {3} to create a token that's a copy of Sorcerer's Broom?"
        ));
    }
}
