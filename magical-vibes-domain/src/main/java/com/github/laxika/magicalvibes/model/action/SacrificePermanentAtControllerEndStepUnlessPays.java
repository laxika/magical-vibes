package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;

import java.util.UUID;

/** Delayed end-step action that asks a permanent's controller to pay before sacrificing it. */
public record SacrificePermanentAtControllerEndStepUnlessPays(
        UUID permanentId,
        UUID controllerId,
        Card sourceCard,
        CostEffect cost
) implements DelayedAction {

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
