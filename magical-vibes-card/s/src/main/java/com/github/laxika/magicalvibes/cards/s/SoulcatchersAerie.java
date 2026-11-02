package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "JUD", collectorNumber = "25")
public class SoulcatchersAerie extends Card {

    public SoulcatchersAerie() {
        // "Whenever a Bird is put into your graveyard from the battlefield": the graveyard's owner, not
        // the permanent's controller, counts. A token is owned by its creator, so tokens use the
        // controller-based slot.
        addTrigger(Triggers.dies(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.NONTOKEN,
                                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.BIRD)))),
                        ObjectRelation.YOU_OWN),
                new PutCountersOnSelfEffect(CounterType.FEATHER));
        addTrigger(Triggers.dies(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.BIRD),
                                new PermanentIsTokenPredicate())),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new PutCountersOnSelfEffect(CounterType.FEATHER));
        addStaticEffect(new StaticBoostEffect(
                1,
                1,
                Set.of(),
                GrantScope.ALL_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.BIRD)),
                CounterType.FEATHER));
    }
}
