package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.filter.OpponentPlayerTargetFilter;

@CardRegistration(set = "10E", collectorNumber = "171")
public class RavenousRats extends Card {

    public RavenousRats() {
        setNeedsTarget(true);
        setTargetFilter(new OpponentPlayerTargetFilter());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TargetPlayerDiscardsEffect(1));
    }
}
