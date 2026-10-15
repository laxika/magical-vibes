package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AnotherPermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "BLB", collectorNumber = "80")
public class WaterspoutWarden extends Card {

    public WaterspoutWarden() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new AnotherPermanentEnteredThisTurn(new CardTypePredicate(CardType.CREATURE)),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
    }
}
