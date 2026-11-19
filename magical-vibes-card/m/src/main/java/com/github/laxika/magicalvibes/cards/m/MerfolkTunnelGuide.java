package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CreateBoonEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "25")
public class MerfolkTunnelGuide extends Card {

    public MerfolkTunnelGuide() {
        PermanentPredicate merfolkCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.MERFOLK),
                new PermanentControlledBySourceControllerPredicate()
        ));
        TargetFilter targetFilter = new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.MERFOLK))),
                "Target must be a Merfolk creature you control");
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateBoonEffect(3, new TriggeredAbility(
                Triggers.enters(new PermanentHasSubtypePredicate(CardSubtype.MERFOLK), ObjectRelation.YOU_CONTROL),
                TriggerOptions.ONE_OR_MORE, new ExploreEffect(true, merfolkCreature)), targetFilter));
    }
}
