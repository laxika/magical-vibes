package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RollD6Effect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "145")
public class VelukanDragon extends Card {

    public VelukanDragon() {
        RollD6Effect roll = new RollD6Effect(List.of(
                new BoostSelfEffect(0, 0),
                new BoostSelfEffect(1, 0),
                new BoostSelfEffect(2, 0),
                new BoostSelfEffect(3, 0),
                new BoostSelfEffect(4, 0),
                new BoostSelfEffect(5, 0)
        ));
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), TriggerOptions.ONE_OR_MORE, roll);
    }
}
