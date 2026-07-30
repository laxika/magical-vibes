package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsCreatureWithGreatestPower;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "AVR", collectorNumber = "198")
public class TriumphOfFerocity extends Card {

    public TriumphOfFerocity() {
        // At the beginning of your upkeep, draw a card if you control the creature with the
        // greatest power or tied for the greatest power. The "if" clause is not an intervening-if
        // (it does not follow the trigger event), so the ability always triggers and the condition
        // is checked on resolution.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new ControlsCreatureWithGreatestPower(),
                new DrawCardEffect(1)));
    }
}
