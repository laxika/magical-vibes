package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "140")
public class VampiricSliver extends Card {

    public VampiricSliver() {
        // All Sliver creatures have the ability to grow when a creature they damaged dies.
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS),
                new PutCountersOnSourceEffect(1, 1, 1)),
                GrantScope.ALL_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS),
                new PutCountersOnSourceEffect(1, 1, 1)),
                GrantScope.SELF,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
