package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DelayedEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "89")
public class FickleEfreet extends Card {

    public FickleEfreet() {
        DelayedEndOfCombatEffect flip = new DelayedEndOfCombatEffect(
                new FlipCoinWinEffect(null, new ChooseOpponentGainsControlOfSourceEffect()));
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), TriggerOptions.ONE_OR_MORE, flip);
    }
}
