package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "TMP", collectorNumber = "177")
@CardRegistration(set = "10E", collectorNumber = "204")
@CardRegistration(set = "9ED", collectorNumber = "188")
@CardRegistration(set = "8ED", collectorNumber = "187")
@CardRegistration(set = "DPA", collectorNumber = "44")
@CardRegistration(set = "HOP", collectorNumber = "55")
public class FurnaceOfRath extends Card {

    public FurnaceOfRath() {
        addEffect(EffectSlot.STATIC, new DoubleDamageEffect());
    }
}
