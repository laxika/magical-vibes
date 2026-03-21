package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.KickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "DOM", collectorNumber = "167")
public class KrosanDruid extends Card {

    public KrosanDruid() {
        // Kicker {4}{G}
        addEffect(EffectSlot.STATIC, new KickerEffect("{4}{G}"));

        // When this creature enters, if it was kicked, you gain 10 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new KickedConditionalEffect(
                new GainLifeEffect(10)
        ));
    }
}
