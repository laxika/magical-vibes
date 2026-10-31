package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect;

@CardRegistration(set = "ROE", collectorNumber = "101")
@CardRegistration(set = "AFC", collectorNumber = "96")
@CardRegistration(set = "C17", collectorNumber = "105")
public class ConsumingVapors extends Card {

    public ConsumingVapors() {
        addSpellEffect(new SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect(false, false));
    }
}
