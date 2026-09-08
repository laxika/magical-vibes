package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifePerSubtypeEffect;

@CardRegistration(set = "RIX", collectorNumber = "64")
public class ChampionOfDusk extends Card {

    public ChampionOfDusk() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawAndLoseLifePerSubtypeEffect(CardSubtype.VAMPIRE));
    }
}
