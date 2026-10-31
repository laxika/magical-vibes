package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "WAR", collectorNumber = "71")
public class TamiyosEpiphany extends Card {

    public TamiyosEpiphany() {
        addSpellEffect(new ScryEffect(4));
        addSpellEffect(new DrawCardEffect(2));
    }
}
