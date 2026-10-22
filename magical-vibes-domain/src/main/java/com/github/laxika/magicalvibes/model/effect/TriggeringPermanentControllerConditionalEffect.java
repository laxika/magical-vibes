package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger-only wrapper for effects that fire when the triggering permanent was controlled by the
 * watcher. With {@code includesSource} false, the watcher's own departure does not trigger it
 * ("another artifact or creature you control", Vat of Rebirth).
 */
public record TriggeringPermanentControllerConditionalEffect(CardEffect wrapped, boolean includesSource)
        implements CardEffect {

    public TriggeringPermanentControllerConditionalEffect(CardEffect wrapped) {
        this(wrapped, true);
    }

    /** Fires for other permanents the watcher's controller controls, but not for the watcher itself. */
    public static TriggeringPermanentControllerConditionalEffect another(CardEffect wrapped) {
        return new TriggeringPermanentControllerConditionalEffect(wrapped, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
