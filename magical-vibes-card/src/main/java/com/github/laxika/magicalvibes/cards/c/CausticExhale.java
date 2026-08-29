package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.BeholdCost;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOwnCastCostUnlessRevealSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TDM", collectorNumber = "74")
public class CausticExhale extends Card {

    public CausticExhale() {
        addEffect(EffectSlot.SPELL, BeholdCost.optional(CardSubtype.DRAGON));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.DRAGON))),
                new IncreaseOwnCastCostUnlessRevealSubtypeEffect(1, CardSubtype.DRAGON)));
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-3, -3));
    }
}
