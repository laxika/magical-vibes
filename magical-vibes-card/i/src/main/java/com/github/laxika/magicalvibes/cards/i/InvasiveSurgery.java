package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "68")
@CardRegistration(set = "SIR", collectorNumber = "76")
public class InvasiveSurgery extends Card {

    public InvasiveSurgery() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.SORCERY_SPELL)),
                "Target must be a sorcery spell."
        )).addSpellEffect(new CounterSpellEffect())
                .addSpellEffect(new ConditionalEffect(new Delirium(), ExileCardsWithSameNameEffect.anyNumber(
                        Subjects.targetSpell(), Subjects.controllerOf(Subjects.targetSpell()))));
    }
}
