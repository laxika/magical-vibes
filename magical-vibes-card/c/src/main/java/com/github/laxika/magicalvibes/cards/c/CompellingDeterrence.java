package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
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
@CardRegistration(set = "SOI", collectorNumber = "52")
@CardRegistration(set = "DDQ", collectorNumber = "42")
@CardRegistration(set = "SIR", collectorNumber = "55")
public class CompellingDeterrence extends Card {

    public CompellingDeterrence() {
        // Bounce first; the Zombie check is made after it, and the permanent's owner (who received the
        // card in hand) discards.
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                "Target must be a nonland permanent"
        )).addSpellEffect(ReturnToHandEffect.target())
                .addSpellEffect(new ConditionalEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)), new DiscardEffect(1, Subjects.ownerOf(Subjects.target()))));
    }
}
