package com.github.laxika.magicalvibes.model.condition;

/**
 * "If you do" / "if it was [sacrificed | destroyed] this way": an earlier step of the resolving
 * entry recorded a departure of that kind in its resolution scratchpad
 * ({@code StackEntry.getResolutionResults()}). A permanent counts wherever it ended up, so a creature
 * sacrificed into exile instead of the graveyard was still sacrificed; one that regenerated or is
 * indestructible was not destroyed. Use it with {@code ConditionalEffect.unless}, since it is only
 * meaningful as the entry resolves.
 */
public record PermanentLeftThisWay(How how) implements Condition {

    /** The kind of departure the condition looks for. */
    public enum How {
        SACRIFICED,
        DESTROYED
    }

    public static PermanentLeftThisWay sacrificed() {
        return new PermanentLeftThisWay(How.SACRIFICED);
    }

    public static PermanentLeftThisWay destroyed() {
        return new PermanentLeftThisWay(How.DESTROYED);
    }

    @Override
    public String conditionName() {
        return how == How.SACRIFICED ? "sacrificed this way" : "destroyed this way";
    }

    @Override
    public String conditionNotMetReason() {
        return how == How.SACRIFICED ? "nothing was sacrificed" : "nothing was destroyed";
    }
}
