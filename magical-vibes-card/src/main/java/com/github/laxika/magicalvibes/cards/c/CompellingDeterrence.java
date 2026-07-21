package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

/**
 * Compelling Deterrence — {1}{U} Instant.
 * Return target nonland permanent to its owner's hand. Then that player discards a card if you
 * control a Zombie.
 */
@CardRegistration(set = "INR", collectorNumber = "57")
public class CompellingDeterrence extends Card {

    public CompellingDeterrence() {
        // Bounce first; thenCondition is checked post-bounce against the caster. TARGET_OWNER routes
        // the discard to the permanent's owner (who received the card in hand).
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                "Target must be a nonland permanent"
        )).addEffect(EffectSlot.SPELL, new ReturnTargetPermanentToHandThenEffect(
                new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                ThenEffectRecipient.TARGET_OWNER,
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE))));
    }
}
