package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "40K", collectorNumber = "64")
public class TombBlade extends Card {

    public TombBlade() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                UnlessPaysEffect.unlessPays(Subjects.triggeringPlayer(), new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "creature", false),
                        new LoseLifeEffect(
                                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.TARGET_PLAYER),
                                Subjects.triggeringPlayer())));

        addUnearth("{6}{B}{B}");
    }
}
