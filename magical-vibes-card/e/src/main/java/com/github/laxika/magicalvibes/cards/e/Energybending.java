package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OwnLandsGainAllBasicLandTypesUntilEndOfTurnEffect;

@CardRegistration(set = "TLA", collectorNumber = "2")
public class Energybending extends Card {

    public Energybending() {
        addSpellEffect(new OwnLandsGainAllBasicLandTypesUntilEndOfTurnEffect());
        addSpellEffect(new DrawCardEffect());
    }
}
