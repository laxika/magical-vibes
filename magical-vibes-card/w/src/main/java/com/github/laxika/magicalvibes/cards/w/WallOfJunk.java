package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "315")
@CardRegistration(set = "DMR", collectorNumber = "240")
public class WallOfJunk extends Card {

    public WallOfJunk() {
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new ReturnSelfToHandAtEndOfCombatEffect());
    }
}
