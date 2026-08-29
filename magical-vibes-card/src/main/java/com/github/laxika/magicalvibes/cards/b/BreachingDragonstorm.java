package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandMayCastOrPutIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "TDM", collectorNumber = "101")
public class BreachingDragonstorm extends Card {

    public BreachingDragonstorm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTopUntilNonlandMayCastOrPutIntoHandEffect(new Fixed(8)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.DRAGON),
                        ReturnToHandEffect.self()));
    }
}
