package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "147")
public class TibaltsRager extends Card {

    public TibaltsRager() {
        addEffect(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(1));
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}",
                List.of(new BoostSelfEffect(2, 0)),
                "{1}{R}: Tibalt's Rager gets +2/+0 until end of turn."));
    }
}
