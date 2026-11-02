package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsCreatureWithGreatestPower;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "198")
@CardRegistration(set = "MB2", collectorNumber = "135")
public class TriumphOfFerocity extends Card {

    public TriumphOfFerocity() {
        // At the beginning of your upkeep, draw a card if you control the creature with the
        // greatest power or tied for the greatest power. The "if" clause is not an intervening-if
        // (it does not follow the trigger event), so the ability always triggers and the condition
        // is checked on resolution.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(
                new ControlsCreatureWithGreatestPower(),
                new DrawCardEffect(1), false));
    }
}
