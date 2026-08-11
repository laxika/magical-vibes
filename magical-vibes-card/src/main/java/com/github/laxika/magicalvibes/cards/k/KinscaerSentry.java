package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ECL", collectorNumber = "22")
public class KinscaerSentry extends Card {

    public KinscaerSentry() {
        addEffect(EffectSlot.ON_ATTACK, new PutCardToBattlefieldEffect(
                new CardTypePredicate(CardType.CREATURE), "creature", true, true,
                false, false, false, true));
    }
}
