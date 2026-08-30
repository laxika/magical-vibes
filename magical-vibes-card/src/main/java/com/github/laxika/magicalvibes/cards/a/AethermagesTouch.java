package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.amount.Fixed;

@CardRegistration(set = "DIS", collectorNumber = "101")
public class AethermagesTouch extends Card {

    public AethermagesTouch() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsEffect(
                new Fixed(4), new Fixed(1), new CardTypePredicate(CardType.CREATURE),
                LookDestination.BOTTOM_OF_LIBRARY, true, LibrarySearchDestination.BATTLEFIELD,
                true, false, null, null, false, 0, false, false, true));
    }
}
