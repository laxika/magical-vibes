package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreaturesUntilSourceLeavesEffect;

@CardRegistration(set = "VOW", collectorNumber = "34")
public class SaviorOfOllenbock extends Card {

    public SaviorOfOllenbock() {
        addEffect(EffectSlot.ON_SELF_TRAINS,
                new ExileTargetCreaturesUntilSourceLeavesEffect(1, false));
    }
}
