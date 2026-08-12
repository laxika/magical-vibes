package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ZEN", collectorNumber = "158")
public class BeastHunt extends Card {

    public BeastHunt() {
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseNToHandRestToGraveyard(
                3, 3, new CardTypePredicate(CardType.CREATURE), true));
    }
}
