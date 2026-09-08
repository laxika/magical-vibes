package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CrumblingSanctuaryDamageReplacementEffect;

@CardRegistration(set = "MMQ", collectorNumber = "292")
public class CrumblingSanctuary extends Card {

    public CrumblingSanctuary() {
        addEffect(EffectSlot.STATIC, new CrumblingSanctuaryDamageReplacementEffect());
    }
}
