package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "FRF", collectorNumber = "14")
public class HonorsReward extends Card {

    public HonorsReward() {
        addSpellEffect(new GainLifeEffect(4));
        addSpellEffect(new BolsterEffect(2));
    }
}
