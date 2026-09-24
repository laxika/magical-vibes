package com.github.laxika.magicalvibes.model.effect;

/** Grants the controller a finite-use boon whose ability resolves whenever its event occurs. */
public record CreateBoonEffect(int uses, CardEffect triggeredEffect) implements CardEffect {

    public CreateBoonEffect {
        if (uses <= 0) {
            throw new IllegalArgumentException("A boon must have at least one use");
        }
    }
}
