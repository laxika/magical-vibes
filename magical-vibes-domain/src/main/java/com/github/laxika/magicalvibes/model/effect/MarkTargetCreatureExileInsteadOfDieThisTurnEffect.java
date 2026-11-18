package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks the target creature so that if it would die this turn, it is exiled instead: a replacement effect
 * (CR 614.1a) the resolution makes as a floating effect on that creature until end of turn
 * ({@code GameData.exileInsteadOfDying}); the damage effects with an "exile it instead" rider make the same one.
 * Place before a damage effect on the same target so lethal damage triggers the replacement.
 * When {@code trackWithSource} is true, the replacement exiles the creature card with the
 * source permanent. Used by Wilt in the Heat and Gut, Fanatical Priestess.
 */
public record MarkTargetCreatureExileInsteadOfDieThisTurnEffect(boolean trackWithSource)
        implements CardEffect {

    public MarkTargetCreatureExileInsteadOfDieThisTurnEffect() {
        this(false);
    }

    public static MarkTargetCreatureExileInsteadOfDieThisTurnEffect withSourceTracking() {
        return new MarkTargetCreatureExileInsteadOfDieThisTurnEffect(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
