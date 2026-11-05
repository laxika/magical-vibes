package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.BoonTrigger;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

/**
 * Grants the controller a finite-use boon whose ability resolves whenever its event occurs.
 *
 * @param filter what the object of the boon's event must be, or {@code null} for any ({@code Boon#filter})
 */
public record CreateBoonEffect(int uses, CardEffect triggeredEffect, BoonTrigger trigger,
                               TargetFilter targetFilter, CardPredicate filter) implements CardEffect {

    public CreateBoonEffect(int uses, CardEffect triggeredEffect, BoonTrigger trigger, TargetFilter targetFilter) {
        this(uses, triggeredEffect, trigger, targetFilter, null);
    }

    public CreateBoonEffect(int uses, CardEffect triggeredEffect) {
        this(uses, triggeredEffect, BoonTrigger.CREATURE_ENTERS, null);
    }

    public static CreateBoonEffect atControllerEndStep(int uses, CardEffect triggeredEffect) {
        return new CreateBoonEffect(uses, triggeredEffect, BoonTrigger.CONTROLLER_END_STEP, null);
    }

    public CreateBoonEffect(int uses, CardEffect triggeredEffect, TargetFilter targetFilter) {
        this(uses, triggeredEffect, BoonTrigger.CREATURE_ENTERS, targetFilter);
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
