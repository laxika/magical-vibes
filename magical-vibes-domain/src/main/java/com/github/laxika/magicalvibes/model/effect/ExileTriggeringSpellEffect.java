package com.github.laxika.magicalvibes.model.effect;

/**
 * Spell-cast trigger marker: the instant or sorcery spell that caused the trigger is exiled instead of
 * going to the graveyard as it resolves, in the way {@link #exile()} names. A spell that is countered or
 * fizzles is never exiled by it, and neither is a spell whose own effects already move it elsewhere.
 *
 * @param exile               how the spell is exiled
 * @param suspendTimeCounters time counters put on the exiled card; positive for
 *                            {@link TriggeringSpellExile#SUSPENDED} and zero otherwise
 * @param yourGraveyardOnly   {@code true} when the Oracle text replaces putting the card into "your
 *                            graveyard", so a spell owned by another player is left alone
 */
public record ExileTriggeringSpellEffect(TriggeringSpellExile exile, int suspendTimeCounters,
                                         boolean yourGraveyardOnly) implements CardEffect {

    public ExileTriggeringSpellEffect(TriggeringSpellExile exile, boolean yourGraveyardOnly) {
        this(exile, 0, yourGraveyardOnly);
    }

    /** Exile with the given number of suspend time counters (Gandalf of the Secret Fire). */
    public static ExileTriggeringSpellEffect withSuspendCounters(int timeCounters, boolean yourGraveyardOnly) {
        return new ExileTriggeringSpellEffect(TriggeringSpellExile.SUSPENDED, timeCounters, yourGraveyardOnly);
    }

    public ExileTriggeringSpellEffect {
        if (exile == null) {
            throw new IllegalArgumentException("exile cannot be null");
        }
        if ((exile == TriggeringSpellExile.SUSPENDED) != (suspendTimeCounters > 0) || suspendTimeCounters < 0) {
            throw new IllegalArgumentException("suspendTimeCounters must be positive exactly for SUSPENDED");
        }
    }
}
