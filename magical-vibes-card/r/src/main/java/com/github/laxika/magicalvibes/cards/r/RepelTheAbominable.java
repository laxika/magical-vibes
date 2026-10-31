package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "EMN", collectorNumber = "38")
public class RepelTheAbominable extends Card {

    public RepelTheAbominable() {
        addSpellEffect(PreventDamageEffect.fromNonHumanSources());
    }
}
