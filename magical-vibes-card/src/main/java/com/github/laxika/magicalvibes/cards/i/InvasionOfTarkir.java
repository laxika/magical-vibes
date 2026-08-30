package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DefiantThundermaw;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RevealAnyNumberOfCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "MOM", collectorNumber = "149")
public class InvasionOfTarkir extends Card {

    public InvasionOfTarkir() {
        setBackFaceCard(new DefiantThundermaw());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RevealAnyNumberOfCardsFromHandEffect(new CardSubtypePredicate(CardSubtype.DRAGON)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new QueueReflexiveAbilityEffect(DealDamageToAnyTargetEffect.toAnyOtherTarget(
                        new Sum(new Fixed(2), new EventValue()))));
    }

    @Override
    public String getBackFaceClassName() {
        return "DefiantThundermaw";
    }
}
