package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "148")
public class ScoutTheBorders extends Card {

    public ScoutTheBorders() {
        // Reveal the top five cards of your library. You may put a creature or land card from
        // among them into your hand. Put the rest into your graveyard.
        CardAnyOfPredicate creatureOrLand = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.LAND)));
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.mayRevealOneToHandRestToGraveyard(5, creatureOrLand));
    }
}
