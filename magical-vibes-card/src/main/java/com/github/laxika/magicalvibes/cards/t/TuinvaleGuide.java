package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "36")
public class TuinvaleGuide extends Card {

    public TuinvaleGuide() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new PermanentEnteredThisTurn(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)), 2),
                new StaticBoostEffect(1, 0, Set.of(Keyword.LIFELINK), GrantScope.SELF)));
    }
}
