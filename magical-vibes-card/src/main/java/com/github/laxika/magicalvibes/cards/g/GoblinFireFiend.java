package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "127")
public class GoblinFireFiend extends Card {

    public GoblinFireFiend() {
        addEffect(EffectSlot.STATIC, new MustBeBlockedIfAbleEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new BoostSelfEffect(1, 0)),
                "{R}: This creature gets +1/+0 until end of turn."));
    }
}
