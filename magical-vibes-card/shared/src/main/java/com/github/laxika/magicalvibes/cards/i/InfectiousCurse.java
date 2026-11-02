package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingEnchantedPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class InfectiousCurse extends Card {

    public InfectiousCurse() {
        addStaticEffect(new ReduceOwnCastCostIfTargetingEnchantedPlayerEffect(1));
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER), SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.ACTIVE_PLAYER),
                new GainLifeEffect(1)));
    }
}
