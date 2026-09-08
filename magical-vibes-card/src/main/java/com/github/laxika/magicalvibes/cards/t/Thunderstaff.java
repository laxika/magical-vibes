package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventFixedDamagePerSourceToControllerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "153")
public class Thunderstaff extends Card {

    public Thunderstaff() {
        addEffect(EffectSlot.STATIC,
                PreventFixedDamagePerSourceToControllerEffect.fromCreatures(1, true, true));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new BoostAllCreaturesEffect(1, 0, new PermanentIsAttackingPredicate())),
                "{2}, {T}: Attacking creatures get +1/+0 until end of turn."
        ));
    }
}
