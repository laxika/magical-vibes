package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "MBS", collectorNumber = "46")
public class MassacreWurm extends Card {

    public MassacreWurm() {
        // ETB: creatures your opponents control get -2/-2 until end of turn
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new BoostAllCreaturesEffect(-2, -2,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        // Whenever a creature an opponent controls dies, that player loses 2 life
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES,
                new TargetPlayerLosesLifeEffect(2));
    }
}
