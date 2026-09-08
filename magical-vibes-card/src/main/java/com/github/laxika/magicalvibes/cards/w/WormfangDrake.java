package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChampionCreatureEffect;

@CardRegistration(set = "JUD", collectorNumber = "57")
public class WormfangDrake extends Card {

    public WormfangDrake() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChampionCreatureEffect());
    }
}
