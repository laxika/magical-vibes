package com.github.laxika.magicalvibes.model.effect;

/**
 * Torment of Hailfire: "Repeat the following process X times. Each opponent loses {@code lifeLoss}
 * life unless that player sacrifices a nonland permanent of their choice or discards a card." X comes
 * from the resolving stack entry's {@code xValue}. Each opponent, in APNAP order, chooses one of the
 * three outcomes independently each iteration; a player who can neither sacrifice a nonland permanent
 * nor discard simply loses life (they may always choose to lose life). Resolved by
 * {@code TormentOfHailfireEffectHandler}, which drives the per-opponent penalty choice, sacrifice
 * selection, and discard selection.
 *
 * @param lifeLoss life each opponent loses when they don't sacrifice or discard (3 for the printed card)
 */
public record TormentOfHailfireEffect(int lifeLoss) implements CardEffect {
}
