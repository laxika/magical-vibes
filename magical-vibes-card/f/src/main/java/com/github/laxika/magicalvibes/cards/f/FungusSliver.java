package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "195")
@CardRegistration(set = "H09", collectorNumber = "21")
@CardRegistration(set = "TSR", collectorNumber = "203")
public class FungusSliver extends Card {

    public FungusSliver() {
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);
        GrantTriggeredAbilityEffect damagedSliverGetsCounter = new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.dealtDamage(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)),
                Subjects.allCreatures(),
                sliver);

        addStaticEffect(damagedSliverGetsCounter);
    }
}
