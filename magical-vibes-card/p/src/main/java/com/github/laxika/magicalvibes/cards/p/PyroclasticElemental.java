package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "296")
public class PyroclasticElemental extends Card {

    public PyroclasticElemental() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{R}",
                List.of(DealDamageEffect.to(Subjects.targetPlayer(), 1)),
                "{1}{R}{R}: This creature deals 1 damage to target player."));
    }
}
