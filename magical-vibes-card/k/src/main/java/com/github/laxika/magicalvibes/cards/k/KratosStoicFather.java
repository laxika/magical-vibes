package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "2213")
public class KratosStoicFather extends Card {

    public KratosStoicFather() {
        addTrigger(Triggers.youAttack(), new ConditionalEffect(
                new HasAttacker(new PermanentHasSubtypePredicate(CardSubtype.GOD)),
                new ExperienceCountersEffect(1)));

        addTrigger(Triggers.creatureDies(new PermanentHasSubtypePredicate(CardSubtype.GOD), ObjectRelation.ANY),
                new ExperienceCountersEffect(1));

        target(TargetFilters.creature()).addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new PutCounterOnTargetPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, new ControllerExperienceCounters()));
    }
}
