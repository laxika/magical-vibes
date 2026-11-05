package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALL", collectorNumber = "92a")
@CardRegistration(set = "ALL", collectorNumber = "92b")
public class GiftOfTheWoods extends Card {

    public GiftOfTheWoods() {
        // Enchant creature
        target(TargetFilters.creature());

        // Whenever enchanted creature blocks or becomes blocked, it gets +0/+3 until end of turn
        // and you gain 1 life. No "by a creature" clause, so it triggers once per combat event
        // regardless of how many creatures are involved.
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE,
                new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(0), new Fixed(3)),
                new GainLifeEffect(1));
        addTrigger(Triggers.becomesBlocked(),
                new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(0), new Fixed(3)),
                new GainLifeEffect(1));
    }
}
