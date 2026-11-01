package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class DeadlyDancer extends Card {

    public DeadlyDancer() {
        PermanentAllOfPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));

        addTrigger(Triggers.transformsToBackFace(ObjectRelation.THIS),
                new AwardPersistentManaEffect(ManaColor.RED, new Fixed(2)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{R}",
                List.of(new BoostSelfEffect(1, 0), new BoostTargetCreatureEffect(1, 0, anotherCreature)),
                "{R}{R}: This creature and another target creature each get +1/+0 until end of turn.",
                new PermanentPredicateTargetFilter(anotherCreature, "Target must be another creature")
        ));
    }
}
