package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveSourceFromCombatAndUnblockSoleBlockersEffect;

@CardRegistration(set = "ME1", collectorNumber = "112")
public class YdwenEfreet extends Card {

    public YdwenEfreet() {
        addEffect(EffectSlot.ON_BLOCK, new FlipCoinWinEffect(
                null,
                new RemoveSourceFromCombatAndUnblockSoleBlockersEffect()));
    }
}
