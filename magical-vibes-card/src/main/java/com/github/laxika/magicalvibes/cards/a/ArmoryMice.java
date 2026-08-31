package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "WOE", collectorNumber = "3")
public class ArmoryMice extends Card {

    public ArmoryMice() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new PermanentEnteredThisTurn(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)), 2),
                new StaticBoostEffect(0, 2, GrantScope.SELF)));
    }
}
