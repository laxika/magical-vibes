package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "34")
public class PotionOfHealing extends Card {

    public PotionOfHealing() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                "{W}, {T}, Sacrifice this artifact: You gain 3 life."
        ));
    }
}
