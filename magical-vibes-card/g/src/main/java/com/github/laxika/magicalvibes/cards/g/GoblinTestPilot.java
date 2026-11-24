package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "74")
public class GoblinTestPilot extends Card {

    public GoblinTestPilot() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(DealDamageToAnyTargetEffect.toAnyTargetChosenAtRandom(new Fixed(2))),
                "{T}: Goblin Test Pilot deals 2 damage to any target chosen at random."));
    }
}
