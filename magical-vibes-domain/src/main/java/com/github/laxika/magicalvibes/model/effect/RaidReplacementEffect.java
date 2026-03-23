package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper that picks between a base effect and an upgraded effect based on raid.
 * At resolution time, if the controller attacked with a creature this turn,
 * resolves {@code raidEffect}; otherwise resolves {@code baseEffect}.
 * Targeting delegates to both inner effects so target selection works for either path.
 */
public record RaidReplacementEffect(CardEffect baseEffect, CardEffect raidEffect) implements ReplacementConditionalEffect {

    @Override
    public CardEffect upgradedEffect() {
        return raidEffect;
    }

    @Override
    public String conditionName() {
        return "raid";
    }

    @Override
    public boolean canTargetPlayer() {
        return baseEffect.canTargetPlayer() || raidEffect.canTargetPlayer();
    }

    @Override
    public boolean canTargetPermanent() {
        return baseEffect.canTargetPermanent() || raidEffect.canTargetPermanent();
    }

    @Override
    public boolean canTargetSpell() {
        return baseEffect.canTargetSpell() || raidEffect.canTargetSpell();
    }

    @Override
    public boolean canTargetGraveyard() {
        return baseEffect.canTargetGraveyard() || raidEffect.canTargetGraveyard();
    }
}
