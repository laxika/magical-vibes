package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "23")
public class ElectricSeaweed extends Card {

    public ElectricSeaweed() {
        // When this creature enters, until end of turn, whenever another creature dies, this
        // creature deals 1 damage to each non-Wall creature.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new RegisterGlobalTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANOTHER),
                DealDamageEffect.toEach(
                        Subjects.controlledBy(Subjects.eachPlayer(),
                                new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(
                                        new PermanentHasSubtypePredicate(CardSubtype.WALL))))), 1))));

        // {T}: This creature deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(DealDamageEffect.toAnyTarget(1)),
                "{T}: Electric Seaweed deals 1 damage to any target."));
    }
}
