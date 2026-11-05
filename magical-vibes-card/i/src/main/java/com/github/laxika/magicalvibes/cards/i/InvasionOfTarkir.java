package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DefiantThundermaw;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RevealAnyNumberOfCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "149")
public class InvasionOfTarkir extends Card {

    public InvasionOfTarkir() {
        setBackFaceCard(new DefiantThundermaw());

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new RevealAnyNumberOfCardsFromHandEffect(new CardSubtypePredicate(CardSubtype.DRAGON)),
                new QueueReflexiveAbilityEffect(DealDamageToAnyTargetEffect.toAnyOtherTarget(
                        new Sum(new Fixed(2), new EventValue()))));
    }

    @Override
    public String getBackFaceClassName() {
        return "DefiantThundermaw";
    }
}
