package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersHaveNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopPlanarCardMayPutOnBottomEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPCA", collectorNumber = "73")
public class StairsToInfinity extends Card {
    public StairsToInfinity() {
        addStaticEffect(new PlayersHaveNoMaximumHandSizeEffect());
        addTrigger(Triggers.youRollOneOrMoreDice(), new DrawCardEffect(1));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new RevealTopPlanarCardMayPutOnBottomEffect());
    }
}
