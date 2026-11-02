package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "269")
@CardRegistration(set = "BTD", collectorNumber = "68")
public class SegmentedWurm extends Card {

    public SegmentedWurm() {
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(), new PutCountersOnSourceEffect(-1, -1, 1));
    }
}
