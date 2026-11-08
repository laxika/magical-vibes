package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentToughness;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "24")
@CardRegistration(set = "FDC", collectorNumber = "43")
@CardRegistration(set = "KHM", collectorNumber = "338")
public class RighteousValkyrie extends Card {

    public RighteousValkyrie() {
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.ANGEL),
                                        new PermanentHasSubtypePredicate(CardSubtype.CLERIC))))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new GainLifeEffect(new TriggeringPermanentToughness()));
        addStaticEffect(new ConditionalEffect(
                        new ControllerLifeAtLeast(7, true),
                        new StaticBoostEffect(2, 2, GrantScope.ALL_OWN_CREATURES)));
    }
}
