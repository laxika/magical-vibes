package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

@CardRegistration(set = "ECL", collectorNumber = "70")
public class SilvergillPeddler extends Card {

    public SilvergillPeddler() {
        // Whenever this creature becomes tapped, draw a card, then discard a card.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED, new TriggeringPermanentConditionalEffect(
                new PermanentIsSourceCardPredicate(),
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER))));
    }
}
