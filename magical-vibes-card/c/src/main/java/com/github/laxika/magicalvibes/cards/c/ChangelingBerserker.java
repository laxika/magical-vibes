package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChampionCreatureEffect;

@CardRegistration(set = "LRW", collectorNumber = "160")
public class ChangelingBerserker extends Card {

    public ChangelingBerserker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChampionCreatureEffect());
    }
}
