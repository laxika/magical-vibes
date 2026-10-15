package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "EMN", collectorNumber = "20")
public class DeployTheGatewatch extends Card {

    public DeployTheGatewatch() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsEffect(
                new Fixed(7),
                new Fixed(2),
                new CardTypePredicate(CardType.PLANESWALKER),
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM,
                false,
                LibrarySearchDestination.BATTLEFIELD,
                true));
    }
}
