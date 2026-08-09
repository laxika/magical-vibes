package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "BOK", collectorNumber = "124")
public class EnshrinedMemories extends Card {

    public EnshrinedMemories() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsEffect(
                new XValue(), new XValue(), new CardTypePredicate(CardType.CREATURE),
                LookDestination.BOTTOM_OF_LIBRARY, true));
    }
}
