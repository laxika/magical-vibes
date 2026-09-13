package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersHaveNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopPlanarCardMayPutOnBottomEffect;

@CardRegistration(set = "OPCA", collectorNumber = "73")
public class StairsToInfinity extends Card {
    public StairsToInfinity() {
        addEffect(EffectSlot.STATIC, new PlayersHaveNoMaximumHandSizeEffect());
        addEffect(EffectSlot.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE, new DrawCardEffect(1));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new RevealTopPlanarCardMayPutOnBottomEffect());
    }
}
