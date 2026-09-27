package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Marker for land-entry trigger conditions that inspect the entering permanent.
 * Implementations are evaluated by the landfall trigger collector and are unwrapped
 * before the resulting ability is put on the stack.
 */
public interface EnteringLandConditionalEffect extends CardEffect {

    CardEffect wrapped();

    boolean testEnteringPermanent(Permanent enteringPermanent);

    String triggerDescription(Card enteringLand);

    @Override
    default TargetSpec targetSpec() {
        return wrapped().targetSpec();
    }
}
