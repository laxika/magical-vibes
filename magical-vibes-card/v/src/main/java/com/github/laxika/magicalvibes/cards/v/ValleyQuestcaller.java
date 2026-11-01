package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "36")
public class ValleyQuestcaller extends Card {

    public ValleyQuestcaller() {
        Set<CardSubtype> valleyCreatures = Set.of(
                CardSubtype.RABBIT, CardSubtype.BAT, CardSubtype.BIRD, CardSubtype.MOUSE);
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(valleyCreatures)));
        addTrigger(new TriggerCondition(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasAnySubtypePredicate(valleyCreatures),
                        new ScryEffect(1)));
    }
}
