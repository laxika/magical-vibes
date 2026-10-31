package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesGainLifePerCreatureEffect;

@CardRegistration(set = "PTK", collectorNumber = "141")
public class MarshalingTheTroops extends Card {

    public MarshalingTheTroops() {
        addSpellEffect(new TapCreaturesGainLifePerCreatureEffect(4));
    }
}
