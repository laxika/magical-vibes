package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DelayedEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PCY", collectorNumber = "89")
public class FickleEfreet extends Card {

    public FickleEfreet() {
        DelayedEndOfCombatEffect flip = new DelayedEndOfCombatEffect(
                new FlipCoinWinEffect(null, new ChooseOpponentGainsControlOfSourceEffect()));
        addTrigger(Triggers.attacks(), flip);
        addTrigger(Triggers.blocks(), TriggerMode.ONCE_PER_BLOCK, flip);
    }
}
