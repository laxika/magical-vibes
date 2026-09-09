package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "BFZ", collectorNumber = "152")
public class RecklessCohort extends Card {

    public RecklessCohort() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new ControlsAnotherPermanent(new PermanentHasSubtypePredicate(CardSubtype.ALLY))),
                new MustAttackEffect()));
    }
}
