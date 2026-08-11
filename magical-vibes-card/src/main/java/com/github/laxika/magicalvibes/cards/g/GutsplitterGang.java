package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ECL", collectorNumber = "106")
public class GutsplitterGang extends Card {

    public GutsplitterGang() {
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayEffect(new BlightEffect(2, null), "Blight 2?", new LoseLifeEffect(3)));
    }
}
