package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlledCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "188")
public class GladeWatcher extends Card {

    public GladeWatcher() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new CanAttackAsThoughNoDefenderEffect()),
                "{G}: This creature can attack this turn as though it didn't have defender. Activate only if creatures you control have total power 8 or greater."
        ).withActivationCondition(
                new ControlledCreaturesTotalPowerAtLeast(8),
                "Activate only if creatures you control have total power 8 or greater."
        ));
    }
}
