package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Marker for ally-creature-enters conditional effects whose condition can be evaluated
 * from the entering creature's card or permanent.
 * <p>
 * Implementations live in the domain and encode both the test and the log description
 * so the scan service needs only one instanceof branch for all power-gated triggers.
 */
public interface EnterCreatureConditionalEffect extends CardEffect {

    CardEffect wrapped();

    boolean testEnteringCreature(Card enteringCreature);

    default boolean testEnteringPermanent(Permanent enteringPermanent) {
        return enteringPermanent != null && testEnteringCreature(enteringPermanent.getCard());
    }

    String triggerDescription(Card enteringCreature);
}
