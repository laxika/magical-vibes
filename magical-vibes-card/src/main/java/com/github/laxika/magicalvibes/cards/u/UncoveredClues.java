package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "19")
public class UncoveredClues extends Card {

    public UncoveredClues() {
        // Private look at the top four; may reveal up to two instant and/or sorcery cards from
        // among them to hand, the rest go to the bottom of the library in any order.
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.mayRevealUpToToHandRestOnBottom(4, instantOrSorcery, 2));
    }
}
