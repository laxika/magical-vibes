package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "288")
public class PendulumOfPatterns extends Card {

    public PendulumOfPatterns() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(3));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1)),
                "{5}, {T}, Sacrifice this artifact: Draw a card."
        ));
    }
}
