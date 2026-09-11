package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "TMT", collectorNumber = "39")
@CardRegistration(set = "TMT", collectorNumber = "228")
public class DonatellosTechnique extends Card {

    public DonatellosTechnique() {
        addSneak("{U}");
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
