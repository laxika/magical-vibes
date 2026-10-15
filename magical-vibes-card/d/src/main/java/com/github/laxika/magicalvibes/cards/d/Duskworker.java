package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "166")
public class Duskworker extends Card {

    public Duskworker() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new RegenerateEffect());
        addActivatedAbility(new ActivatedAbility(false, "{3}", List.of(new BoostSelfEffect(1, 0)),
                "{3}: Duskworker gets +1/+0 until end of turn."));
    }
}
