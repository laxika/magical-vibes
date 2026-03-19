package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.KickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "DOM", collectorNumber = "80")
public class CaligoSkinWitch extends Card {

    public CaligoSkinWitch() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{3}{B}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new KickedConditionalEffect(new EachOpponentDiscardsEffect(2)));
    }
}
