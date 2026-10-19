package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed triggered ability that sacrifices its source at an end step. Its controller remains the
 * controller of the ability that created it; the sacrifice instruction may instead use the
 * permanent's current controller.
 */
public record SacrificeSelfAtNextEndStepTrigger(
        UUID permanentId,
        UUID controllerId,
        Card sourceCard,
        Integer registeredTurnNumber,
        boolean currentControllerSacrifices,
        boolean currentControllersEndStepOnly
) implements DelayedAction {

    public SacrificeSelfAtNextEndStepTrigger(UUID permanentId, UUID controllerId, Card sourceCard) {
        this(permanentId, controllerId, sourceCard, null, false, false);
    }

    public SacrificeSelfAtNextEndStepTrigger(
            UUID permanentId, UUID controllerId, Card sourceCard, Integer registeredTurnNumber) {
        this(permanentId, controllerId, sourceCard, registeredTurnNumber, false, false);
    }

    public SacrificeSelfAtNextEndStepTrigger(
            UUID permanentId, UUID controllerId, Card sourceCard, boolean currentControllerSacrifices) {
        this(permanentId, controllerId, sourceCard, null, currentControllerSacrifices, false);
    }

    public SacrificeSelfAtNextEndStepTrigger(UUID permanentId, UUID controllerId, Card sourceCard,
                                             boolean currentControllerSacrifices,
                                             boolean currentControllersEndStepOnly) {
        this(permanentId, controllerId, sourceCard, null, currentControllerSacrifices,
                currentControllersEndStepOnly);
    }
}
