package com.github.laxika.magicalvibes.model.condition;

/**
 * True when at least {@code count} creatures other than the one the trigger event names are on the battlefield:
 * Lifeline's intervening "if another creature is on the battlefield" ({@code count} 1) and Portcullis's "if there
 * are two or more other creatures on the battlefield" ({@code count} 2), checked as the ability triggers and again as
 * it resolves (CR 603.4). As a creature's death triggers the ability, the creatures that died with it count, since a
 * leaves-the-battlefield ability looks back in time (CR 603.10a), so Lifeline's ability triggers when every creature
 * dies at once; as it resolves only the battlefield counts, so it then does nothing (Lifeline's ruling).
 *
 * @param count how many other creatures there must be, at least 1
 */
public record AnotherCreatureOnBattlefield(int count) implements Condition {

    public AnotherCreatureOnBattlefield {
        if (count < 1) {
            throw new IllegalArgumentException("count must be at least 1");
        }
    }

    /** "If another creature is on the battlefield". */
    public AnotherCreatureOnBattlefield() {
        this(1);
    }

    @Override
    public String conditionName() {
        return count == 1 ? "another creature is on the battlefield"
                : count + " or more other creatures are on the battlefield";
    }

    @Override
    public String conditionNotMetReason() {
        return count == 1 ? "no other creature is on the battlefield"
                : "fewer than " + count + " other creatures are on the battlefield";
    }
}
