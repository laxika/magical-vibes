package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedUnblockedAttackerGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "84")
@CardRegistration(set = "FEM", collectorNumber = "169")
public class DelifsCone extends Card {

    public DelifsCone() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new RegisterDelayedUnblockedAttackerGainLifeEffect()),
                "{T}, Sacrifice this artifact: This turn, when target creature you control attacks "
                        + "and isn't blocked, you may gain life equal to its power. If you do, it "
                        + "assigns no combat damage this turn.",
                TargetFilters.creatureYouControl()
        ));
    }
}
