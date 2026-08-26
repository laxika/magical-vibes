package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "LCI", collectorNumber = "185")
public class GhaltaStampedeTyrant extends Card {

    public GhaltaStampedeTyrant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                PutCardToBattlefieldEffect.anyNumber(new CardTypePredicate(CardType.CREATURE), "creature"));
    }
}
