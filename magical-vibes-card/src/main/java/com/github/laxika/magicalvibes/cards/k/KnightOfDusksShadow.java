package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantGainLifeEffect;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "96")
public class KnightOfDusksShadow extends Card {

    public KnightOfDusksShadow() {
        addEffect(EffectSlot.STATIC, new OpponentsCantGainLifeEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new BoostSelfEffect(1, 1)),
                "{1}{B}: This creature gets +1/+1 until end of turn."
        ));
    }
}
