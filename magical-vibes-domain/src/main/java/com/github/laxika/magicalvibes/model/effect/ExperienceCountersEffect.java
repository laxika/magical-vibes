package com.github.laxika.magicalvibes.model.effect;

/** Changes the controller's experience-counter total. */
public record ExperienceCountersEffect(int amount) implements CardEffect {

    public ExperienceCountersEffect {
        if (amount <= 0) {
            throw new IllegalArgumentException("Experience counter change must be positive");
        }
    }
}
