package com.github.laxika.magicalvibes.model.condition;

/**
 * The source permanent's resolution-counted ability is resolving for exactly the {@code n}-th
 * time this turn (e.g. Ashling the Pilgrim's "If this is the third time this ability has
 * resolved this turn"). The engine increments
 * {@code GameData.permanentAbilityResolutionsThisTurn} whenever an activated ability whose
 * effects carry this condition resolves (counting resolutions, not activations, so copies of
 * the ability count but activations countered on the stack do not), and this condition is met
 * only when that per-permanent count equals {@code n} — the clause fires on the exact n-th
 * resolution and never on a later one.
 */
public record NthAbilityResolutionThisTurn(int n) implements Condition {

    @Override
    public String conditionName() {
        return "resolved-" + n + "-times-this-turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "this is not the " + ordinal(n) + " time the ability has resolved this turn";
    }

    private static String ordinal(int value) {
        return switch (value) {
            case 1 -> "first";
            case 2 -> "second";
            case 3 -> "third";
            default -> value + "th";
        };
    }
}
