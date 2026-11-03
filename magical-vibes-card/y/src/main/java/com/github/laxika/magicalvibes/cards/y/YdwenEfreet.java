package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveSourceFromCombatAndUnblockSoleBlockersEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ME1", collectorNumber = "112")
@CardRegistration(set = "ARN", collectorNumber = "44")
public class YdwenEfreet extends Card {

    public YdwenEfreet() {
        addTrigger(Triggers.blocks(),
                TriggerOptions.ONE_OR_MORE,
                new FlipCoinWinEffect(null, new RemoveSourceFromCombatAndUnblockSoleBlockersEffect()));
    }
}
