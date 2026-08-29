package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingEnchantedPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

public class InfectiousCurse extends Card {

    public InfectiousCurse() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfTargetingEnchantedPlayerEffect(1));
        addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED, SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.ACTIVE_PLAYER),
                new GainLifeEffect(1)));
    }
}
