package com.github.laxika.magicalvibes.model.effect;

/**
 * Amulet of Quoz's activated ante ability: "Target opponent may ante the top card of their library.
 * If they don't, you flip a coin. If you win the flip, that player loses the game. If you lose the
 * flip, you lose the game."
 *
 * <p>The targeted opponent is the decision maker. Anteing is modelled as the observable single-game
 * zone movement — the top card of that player's library leaves the game (moved to exile), mirroring
 * {@link RebirthAnteEffect}; the permanent, cross-game transfer of card ownership is outside a single
 * game's scope and is not modeled. An opponent with an empty library has no top card to ante, so they
 * can't ante and the coin flip happens immediately.
 *
 * <p>Both loss branches go through the single lose-the-game gate, so "can't lose" effects (Platinum
 * Angel) and loss replacers (Lich's Mirror) apply.
 *
 * <p>Targets the opponent player — the ability declares "target opponent" through a
 * {@code PlayerRelationPredicate}; {@link #targetSpec()} declares the harmful player category.
 */
public record AmuletOfQuozAnteEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }
}
