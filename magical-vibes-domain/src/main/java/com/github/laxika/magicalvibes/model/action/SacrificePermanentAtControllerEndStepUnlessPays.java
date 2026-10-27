package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;

import java.util.UUID;

/**
 * Delayed action for "at the beginning of the next end step" sacrifices: the next end step of any player
 * fires it, and its controller is asked to pay before the permanent is sacrificed. When
 * {@code controllerEndStepOnly} is set, it waits for the next end step of its controller instead ("at the
 * beginning of your next end step") and is left queued through every other player's end step.
 */
public record SacrificePermanentAtControllerEndStepUnlessPays(
        UUID permanentId,
        UUID controllerId,
        Card sourceCard,
        CostEffect cost,
        boolean controllerEndStepOnly
) implements DelayedAction {

    public SacrificePermanentAtControllerEndStepUnlessPays(UUID permanentId, UUID controllerId,
                                                            Card sourceCard, CostEffect cost) {
        this(permanentId, controllerId, sourceCard, cost, false);
    }

    public SacrificePermanentAtControllerEndStepUnlessPays(UUID permanentId, UUID controllerId,
                                                            Card sourceCard, String manaCost) {
        this(permanentId, controllerId, sourceCard, new PayManaCost(manaCost));
    }

    /** Backward-compatible display accessor used by delayed-action logging. */
    public String manaCost() {
        return costDescription();
    }

    public String costDescription() {
        if (cost instanceof PayManaCost payManaCost) {
            return payManaCost.manaCost();
        }
        if (cost instanceof PayEnergyCost payEnergyCost) {
            return payEnergyCost.amount() + " energy";
        }
        return cost.toString();
    }
}
