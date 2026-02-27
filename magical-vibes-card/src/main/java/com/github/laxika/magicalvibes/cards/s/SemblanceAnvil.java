package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForSharedCardTypeWithImprintEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SOM", collectorNumber = "201")
public class SemblanceAnvil extends Card {

    public SemblanceAnvil() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new ExileFromHandToImprintEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)), "a nonland card"),
                        "You may exile a nonland card from your hand."));
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostForSharedCardTypeWithImprintEffect(2));
    }
}
