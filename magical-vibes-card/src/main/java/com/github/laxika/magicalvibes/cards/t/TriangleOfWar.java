package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "158")
public class TriangleOfWar extends Card {

    public TriangleOfWar() {
        // {2}, Sacrifice this artifact: Target creature you control fights target creature an opponent controls.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new FightTargetsEffect()),
                "{2}, Sacrifice this artifact: Target creature you control fights target creature an opponent controls.",
                List.of(
                        TargetFilters.creatureYouControl(),
                        TargetFilters.creatureAnOpponentControls()
                ),
                2,
                2
        ));
    }
}
