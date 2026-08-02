package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "121")
public class WallOfLimbs extends Card {

    public WallOfLimbs() {
        // Whenever you gain life, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        // {5}{B}{B}, Sacrifice this creature: Target player loses X life, where X is this
        // creature's power (snapshotted into xValue when the sacrifice cost is paid).
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{B}{B}",
                List.of(new SacrificeSelfCost(true),
                        new LoseLifeEffect(new XValue(), LoseLifeRecipient.TARGET_PLAYER)),
                "{5}{B}{B}, Sacrifice this creature: Target player loses X life, where X is this "
                        + "creature's power."
        ));
    }
}
