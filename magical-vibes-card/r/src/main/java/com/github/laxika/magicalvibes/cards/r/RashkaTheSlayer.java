package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenCombatOpponentMatchesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "12")
public class RashkaTheSlayer extends Card {

    public RashkaTheSlayer() {
        // Whenever Rashka blocks one or more black creatures, Rashka gets +1/+2 until end of turn.
        addTrigger(Triggers.blocks(),
                TriggerOptions.ONE_OR_MORE,
                new BoostSelfWhenCombatOpponentMatchesEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.BLACK)), 1, 2));
    }
}
