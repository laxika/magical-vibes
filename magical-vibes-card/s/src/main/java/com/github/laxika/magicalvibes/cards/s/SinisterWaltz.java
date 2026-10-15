package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "VOC", collectorNumber = "30")
@CardRegistration(set = "VOC", collectorNumber = "68")
public class SinisterWaltz extends Card {

    public SinisterWaltz() {
        addEffect(EffectSlot.SPELL, ReturnTargetCardsFromGraveyardToBattlefieldEffect.sinisterWaltz(
                new CardTypePredicate(CardType.CREATURE)));
    }
}
