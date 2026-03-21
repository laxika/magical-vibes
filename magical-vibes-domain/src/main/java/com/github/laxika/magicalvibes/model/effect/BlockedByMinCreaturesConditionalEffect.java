package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for conditional static effects that only apply when the source creature
 * is being blocked by at least {@code minBlockers} creatures.
 * <p>
 * Example: Rampaging Cyclops gets -2/-0 as long as two or more creatures are blocking it.
 */
public record BlockedByMinCreaturesConditionalEffect(int minBlockers, CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "blocked by " + minBlockers + "+ creatures";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minBlockers + " creatures blocking";
    }

    @Override
    public boolean canTargetPlayer() {
        return wrapped.canTargetPlayer();
    }

    @Override
    public boolean canTargetPermanent() {
        return wrapped.canTargetPermanent();
    }

    @Override
    public boolean canTargetSpell() {
        return wrapped.canTargetSpell();
    }

    @Override
    public boolean canTargetGraveyard() {
        return wrapped.canTargetGraveyard();
    }
}
