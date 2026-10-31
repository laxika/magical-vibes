package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "GPT", collectorNumber = "21")
public class Withstand extends Card {

    public Withstand() {
        addSpellEffect(PreventDamageEffect.nextToTarget(3));
        addSpellEffect(new DrawCardEffect(1));
    }
}
