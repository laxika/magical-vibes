package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "184")
public class LairDelve extends Card {

    public LairDelve() {
        // Reveal the top two cards; every creature or land card revealed goes to hand, the rest on
        // the bottom in any order. chooseCount == lookCount makes it choice-free — all eligible
        // cards auto-move to hand.
        CardAnyOfPredicate creatureOrLand = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.LAND)));

        addEffect(EffectSlot.SPELL, new LookAtTopCardsEffect(
                new Fixed(2), new Fixed(2), creatureOrLand,
                LookDestination.BOTTOM_OF_LIBRARY, true));
    }
}
