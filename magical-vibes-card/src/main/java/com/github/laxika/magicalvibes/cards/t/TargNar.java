package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttackingCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleSelfPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "234")
public class TargNar extends Card {

    public TargNar() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new AttackingCreaturesTotalPowerAtLeast(6),
                new BoostAllOwnCreaturesEffect(1, 0, new PermanentIsAttackingPredicate())));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}{G}",
                List.of(new DoubleSelfPowerToughnessEffect()),
                "{2}{R}{G}: Double Targ Nar's power and toughness until end of turn."
        ));
    }
}
