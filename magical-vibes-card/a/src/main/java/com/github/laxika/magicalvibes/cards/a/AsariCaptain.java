package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "215")
public class AsariCaptain extends Card {

    public AsariCaptain() {
        Set<CardSubtype> samuraiOrWarrior = Set.of(CardSubtype.SAMURAI, CardSubtype.WARRIOR);
        PermanentHasAnySubtypePredicate samuraiOrWarriorPredicate =
                new PermanentHasAnySubtypePredicate(samuraiOrWarrior);
        PermanentCount samuraiOrWarriorsYouControl =
                new PermanentCount(samuraiOrWarriorPredicate, CountScope.CONTROLLER);

        addTrigger(Triggers.creatureYouControlAttacks().withFilter(samuraiOrWarriorPredicate),
                TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                new BoostTargetCreatureEffect(samuraiOrWarriorsYouControl, new Fixed(0)));
    }
}
