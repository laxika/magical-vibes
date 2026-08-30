package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MID", collectorNumber = "56")
public class Geistwave extends Card {

    public Geistwave() {
        // Return target nonland permanent to its owner's hand. If you controlled that permanent,
        // draw a card. Control is checked before the target leaves the battlefield.
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, new ReturnTargetPermanentToHandThenEffect(
                        new DrawCardEffect(1),
                        ThenEffectRecipient.CONTROLLER,
                        null,
                        new TargetPermanentMatches(new PermanentControlledBySourceControllerPredicate())));
    }
}
