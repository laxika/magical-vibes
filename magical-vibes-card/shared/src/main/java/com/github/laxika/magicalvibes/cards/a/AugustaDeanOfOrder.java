package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class AugustaDeanOfOrder extends Card {

    public AugustaDeanOfOrder() {
        PermanentIsCreaturePredicate creature = new PermanentIsCreaturePredicate();
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.otherTappedCreaturesYouControl()));
        addStaticEffect(new StaticBoostEffect(0, 1, Subjects.otherUntappedCreaturesYouControl()));
        addTrigger(Triggers.youAttack(), SequenceEffect.of(
                new UntapPermanentsEffect(Subjects.controlledBy(Subjects.you(), creature)),
                new TapAnyNumberPermanentsEffect(creature)));
    }
}
