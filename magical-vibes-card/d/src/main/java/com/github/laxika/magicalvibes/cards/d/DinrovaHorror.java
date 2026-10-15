package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

/**
 * Dinrova Horror — {4}{U}{B} Creature — Horror 4/4.
 * When this creature enters, return target permanent to its owner's hand, then that player
 * discards a card.
 */
@CardRegistration(set = "GTC", collectorNumber = "155")
@CardRegistration(set = "MM3", collectorNumber = "160")
@CardRegistration(set = "GK1", collectorNumber = "14")
@CardRegistration(set = "PIO", collectorNumber = "215")
public class DinrovaHorror extends Card {

    public DinrovaHorror() {
        // Any permanent is a legal target. The discard is caused by this ability's controller, so the
        // then-effect keeps that controller as the causer and makes the permanent's owner the player
        // who discards (so an opponent-caused discard applies Loxodon Smiter's replacement).
        target(new PermanentPredicateTargetFilter(new PermanentTruePredicate(),
                "Target must be a permanent"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnTargetPermanentToHandThenEffect(
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                        ThenEffectRecipient.TARGET_OWNER_AS_TARGET));
    }
}
