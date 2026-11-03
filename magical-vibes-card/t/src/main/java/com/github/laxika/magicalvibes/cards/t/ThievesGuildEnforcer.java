package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "125")
@CardRegistration(set = "M21", collectorNumber = "361")
public class ThievesGuildEnforcer extends Card {

    public ThievesGuildEnforcer() {
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.ROGUE))),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                new MillEffect(2, MillRecipient.EACH_OPPONENT));
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.ROGUE),
                                new PermanentNotPredicate(new PermanentIsCreaturePredicate()))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new MillEffect(2, MillRecipient.EACH_OPPONENT));
        addStaticEffect(new ConditionalEffect(
                        new OpponentGraveyardAtLeast(8),
                        new StaticBoostEffect(2, 1, Set.of(Keyword.DEATHTOUCH), GrantScope.SELF)));
    }
}
