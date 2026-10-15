package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "ECL", collectorNumber = "210")
public class ChaosSpewer extends Card {

    public ChaosSpewer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayPayManaEffect(
                "{2}",
                null,
                "Pay {2}?",
                new BlightEffect(2, null)));
    }
}
