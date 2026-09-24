package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealUntilChosenCreatureTypeCountToBattlefieldEffect;

@CardRegistration(set = "SLD", collectorNumber = "1901")
public class KindredSummons extends Card {

    public KindredSummons() {
        addEffect(EffectSlot.SPELL, new RevealUntilChosenCreatureTypeCountToBattlefieldEffect());
    }
}
