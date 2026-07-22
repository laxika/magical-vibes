package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "199")
public class GrappleWithThePast extends Card {

    public GrappleWithThePast() {
        // Mill three cards, then you may return a creature or land card from your
        // graveyard to your hand. SequenceEffect so mill runs before the may, and
        // milled creature/land cards are legal returns.
        addEffect(EffectSlot.SPELL, SequenceEffect.of(
                new MillEffect(3, MillRecipient.CONTROLLER),
                new MayEffect(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardTypePredicate(CardType.LAND))))
                                .build(),
                        "Return a creature or land card from your graveyard to your hand?")));
    }
}
