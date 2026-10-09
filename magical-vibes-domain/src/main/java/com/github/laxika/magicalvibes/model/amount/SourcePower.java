package com.github.laxika.magicalvibes.model.amount;

/**
 * The source permanent's effective power, using its last-known snapshot after departure.
 * By default negative power evaluates to zero; effects that apply power as a modifier can
 * retain a negative value with {@code allowNegative}.
 */
public record SourcePower(boolean allowNegative, boolean baseOnly) implements DynamicAmount {

    public SourcePower(boolean allowNegative) {
        this(allowNegative, false);
    }

    public SourcePower() {
        this(false, false);
    }
}
