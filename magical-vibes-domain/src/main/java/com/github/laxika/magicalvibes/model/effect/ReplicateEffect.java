package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger marker for a spell with replicate. The cast-time trigger collector counts the matching
 * repeatable additional-cost payments and creates one copy of the spell for each payment.
 *
 * @param manaCost the replicate payment that identifies the spell's repeatable additional cost
 */
public record ReplicateEffect(String manaCost) implements CardEffect {

    public ReplicateEffect {
        if (manaCost == null || manaCost.isBlank()) {
            throw new IllegalArgumentException("manaCost must not be blank");
        }
    }
}
