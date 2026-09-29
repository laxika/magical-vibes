package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.BoonTrigger;

/** Grants the controller a finite-use boon whose ability resolves whenever its event occurs. */
public record CreateBoonEffect(int uses, CardEffect triggeredEffect, BoonTrigger trigger) implements CardEffect {

    public CreateBoonEffect(int uses, CardEffect triggeredEffect) {
        this(uses, triggeredEffect, BoonTrigger.CREATURE_ENTERS);
    }

    public static CreateBoonEffect atControllerEndStep(int uses, CardEffect triggeredEffect) {
        return new CreateBoonEffect(uses, triggeredEffect, BoonTrigger.CONTROLLER_END_STEP);
    }

    public CreateBoonEffect {
        if (uses <= 0) {
            throw new IllegalArgumentException("A boon must have at least one use");
        }
        if (trigger == null) {
            throw new IllegalArgumentException("A boon trigger is required");
        }
    }
}
