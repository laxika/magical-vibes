package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger marker for a spell with replicate. The cast-time trigger collector counts the matching
 * repeatable additional-cost payments and creates one copy of the spell for each payment.
 *
 * @param manaCost the replicate payment that identifies the spell's repeatable additional cost
 * @param tokenCopy whether copies of a permanent spell enter the battlefield as tokens
 */
public record ReplicateEffect(String manaCost, boolean tokenCopy) implements CardEffect {

    public ReplicateEffect(String manaCost) {
        this(manaCost, false);
    }

    public ReplicateEffect {
        if (manaCost == null || manaCost.isBlank()) {
            throw new IllegalArgumentException("manaCost must not be blank");
        }
    }
}
