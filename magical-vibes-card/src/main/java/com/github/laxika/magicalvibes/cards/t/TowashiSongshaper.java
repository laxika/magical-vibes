package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "NEO", collectorNumber = "167")
public class TowashiSongshaper extends Card {

    public TowashiSongshaper() {
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, new BoostSelfEffect(1, 0));
    }
}
