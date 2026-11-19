package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * A player reveals cards from the top of their library until {@code landCount} land cards are
 * revealed (or the library empties). That player puts all cards revealed this way into their
 * graveyard.
 * <p>
 * {@code player} selects who reveals. {@code targetPlayer()} (the default) declares a harmful player target — Mind
 * Funeral ({@code landCount = 4}, the card restricts the choice to an opponent). {@code controllerOf(target())}
 * reveals for the controller of the permanent a sibling effect targets (as it is, or as it last was once that effect
 * destroyed it) — Destroy the Evidence ({@code landCount = 1}). {@code eachOpponent()} reveals for every opponent, one
 * after another in APNAP order — Consuming Aberration ({@code landCount = 1}), Mind Grind
 * ({@code landCount = new XValue()}).
 * <p>
 * {@code landCount} is a {@link DynamicAmount} so the land count can come from the spell's paid X.
 */
public record RevealUntilLandsMillTargetPlayerEffect(DynamicAmount landCount, PlayerSubject player)
        implements CardEffect {

    public RevealUntilLandsMillTargetPlayerEffect(int landCount) {
        this(new Fixed(landCount), Subjects.targetPlayer());
    }

    public RevealUntilLandsMillTargetPlayerEffect(int landCount, PlayerSubject player) {
        this(new Fixed(landCount), player);
    }

    public RevealUntilLandsMillTargetPlayerEffect(DynamicAmount landCount) {
        this(landCount, Subjects.targetPlayer());
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget()
                ? TargetSpec.harmful(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
