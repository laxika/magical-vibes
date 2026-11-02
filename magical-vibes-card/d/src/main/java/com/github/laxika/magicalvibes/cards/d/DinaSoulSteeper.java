package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "178")
@CardRegistration(set = "MUL", collectorNumber = "37")
@CardRegistration(set = "MUL", collectorNumber = "102")
@CardRegistration(set = "MUL", collectorNumber = "167")
@CardRegistration(set = "SOC", collectorNumber = "305")
public class DinaSoulSteeper extends Card {

    public DinaSoulSteeper() {
        addTrigger(Triggers.youGainLife(),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeCreatureCost(false, true, false, true),
                        new BoostSelfEffect(new XValue(), new Fixed(0))
                ),
                "{1}, Sacrifice another creature: Dina, Soul Steeper gets +X/+0 until end of turn, "
                        + "where X is the sacrificed creature's power."
        ));
    }
}
