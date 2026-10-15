package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SeekCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "YMID", collectorNumber = "55")
public class SettleTheWilds extends Card {

    public SettleTheWilds() {
        addEffect(EffectSlot.SPELL,
                new SeekCardToBattlefieldEffect(CardPredicateUtils.basicLand(), true));
        addEffect(EffectSlot.SPELL, new SeekCardsToHandEffect(
                new Fixed(1),
                new CardIsPermanentPredicate(),
                new ManaValueBound(
                        new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER),
                        true, 0)));
    }
}
