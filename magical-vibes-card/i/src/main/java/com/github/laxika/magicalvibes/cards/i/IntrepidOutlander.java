package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttackingCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "191")
public class IntrepidOutlander extends Card {

    public IntrepidOutlander() {
        addTrigger(Triggers.attacks(), new ConditionalEffect(
                new AttackingCreaturesTotalPowerAtLeast(6),
                new VentureIntoDungeonEffect()));
    }
}
