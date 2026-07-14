package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;

@CardRegistration(set = "10E", collectorNumber = "343")
@CardRegistration(set = "M10", collectorNumber = "220")
@CardRegistration(set = "9ED", collectorNumber = "309")
@CardRegistration(set = "8ED", collectorNumber = "314")
@CardRegistration(set = "7ED", collectorNumber = "318")
public class Spellbook extends Card {

    public Spellbook() {
        addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());
    }
}
