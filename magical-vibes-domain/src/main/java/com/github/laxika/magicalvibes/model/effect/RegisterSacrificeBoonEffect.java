package com.github.laxika.magicalvibes.model.effect;

/** Gives the controller a finite boon that triggers when they sacrifice a permanent. */
public record RegisterSacrificeBoonEffect(int uses, CardEffect triggeredEffect) implements CardEffect {
}
