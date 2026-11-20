package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;


@CardRegistration(set = "TSP", collectorNumber = "88")
@CardRegistration(set = "SLD", collectorNumber = "1774")
public class Trickbind extends Card {

    public Trickbind() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(StackEntryType.ABILITIES),
                "Target must be an activated or triggered ability."))
                .addSpellEffect(new CounterSpellEffect());
        // If a permanent's ability is countered this way, activated abilities of that permanent can't be
        // activated this turn.
        addSpellEffect(new ConditionalEffect(HappenedThisWay.countered(), LockTargetPermanentEffect.activatedAbilitiesOf(
                Subjects.targetAbilitySource(), Duration.UNTIL_END_OF_TURN)));
    }
}
