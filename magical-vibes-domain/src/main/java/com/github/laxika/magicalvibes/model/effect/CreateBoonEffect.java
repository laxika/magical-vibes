package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;

/** Grants the controller a finite-use boon whose ability resolves whenever its event occurs. */
public record CreateBoonEffect(int uses, CardEffect triggeredEffect, TargetFilter targetFilter) implements CardEffect {

    public CreateBoonEffect(int uses, CardEffect triggeredEffect) {
        this(uses, triggeredEffect, null);
    }

    public CreateBoonEffect {
        if (uses <= 0) {
            throw new IllegalArgumentException("A boon must have at least one use");
        }
    }
}
