package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "TDM", collectorNumber = "27")
public class StormbeaconBlade extends Card {

    public StormbeaconBlade() {
        addStaticEffect(new StaticBoostEffect(3, 0, GrantScope.EQUIPPED_CREATURE));
        // "Draw a card if you control three or more attacking creatures" is the effect's "if", checked as it resolves.
        addTrigger(Triggers.attacks(),
                new ConditionalEffect(new MinimumMatchingAttackers(3, new PermanentTruePredicate()),
                        new DrawCardEffect()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
