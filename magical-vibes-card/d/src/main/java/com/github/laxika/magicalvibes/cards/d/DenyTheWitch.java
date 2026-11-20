package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;


@CardRegistration(set = "40K", collectorNumber = "116")
public class DenyTheWitch extends Card {

    public DenyTheWitch() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(StackEntryType.SPELLS_AND_ABILITIES),
                "Target must be a spell, activated ability, or triggered ability."
        ))
                .addSpellEffect(new LoseLifeEffect(
                        new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER), Subjects.controllerOf(Subjects.targetSpell())))
                .addSpellEffect(new CounterSpellEffect());
    }
}
