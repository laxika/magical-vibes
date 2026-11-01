package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnduringRenewalDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithOwnHandRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "23")
@CardRegistration(set = "TSB", collectorNumber = "7")
public class EnduringRenewal extends Card {

    public EnduringRenewal() {
        // Play with your hand revealed.
        addStaticEffect(new PlayWithOwnHandRevealedEffect());
        // If you would draw a card, reveal the top card of your library instead. If it's a creature
        // card, put it into your graveyard. Otherwise, draw a card.
        addStaticEffect(new EnduringRenewalDrawReplacementEffect());
        // Whenever a creature is put into your graveyard from the battlefield, return it to your hand.
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_OWN),
                new ReturnTriggeringCardToOwnerHandEffect());
    }
}
