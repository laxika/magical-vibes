package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "55")
public class SlitheringShade extends Card {

    public SlitheringShade() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new BoostSelfEffect(1, 1)),
                "{B}: This creature gets +1/+1 until end of turn."
        ));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerHandEmpty(),
                new CanAttackAsThoughNoDefenderEffect()
        ));
    }
}
