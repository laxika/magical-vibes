package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayerWithMostLifeGainsControlOfSourceCreatureEffect;

@CardRegistration(set = "5ED", collectorNumber = "298")
public class GhazbNOgre extends Card {

    public GhazbNOgre() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PlayerWithMostLifeGainsControlOfSourceCreatureEffect());
    }
}
