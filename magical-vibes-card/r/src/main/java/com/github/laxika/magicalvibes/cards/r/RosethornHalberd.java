package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "175")
public class RosethornHalberd extends Card {

    public RosethornHalberd() {
        PermanentPredicate nonHuman = new PermanentNotPredicate(
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN));
        PermanentPredicate nonHumanCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                nonHuman
        ));
        ControlledPermanentPredicateTargetFilter targetFilter = new ControlledPermanentPredicateTargetFilter(
                nonHumanCreature,
                "Target must be a non-Human creature you control");

        target(targetFilter).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new AttachSourceEquipmentToTargetCreatureEffect());
        addStaticEffect(new StaticBoostEffect(2, 1, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{5}", new PermanentIsCreaturePredicate(),
                "Target must be a creature you control"));
    }
}
