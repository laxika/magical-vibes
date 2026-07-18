package com.github.laxika.magicalvibes.model.effect;

/**
 * Siren's Call. On resolution (only ever during an opponent's turn, before attackers are declared)
 * every creature the active player controls must attack this turn if able. Then, at the beginning
 * of the next end step, all non-Wall creatures that player controls that didn't attack this turn are
 * destroyed — but a creature the active player didn't control continuously since the beginning of the
 * turn (i.e. summoning sick) is ignored by that end-step destruction.
 *
 * <p>Non-targeted. The forced attack is applied by setting the transient "must attack this turn" flag
 * on each of the active player's creatures (cleared at end of turn); the end-step destruction is
 * scheduled via a {@code DestroyNonAttackersAtEndStep} delayed action keyed on the active player.
 */
public record SirensCallEffect() implements CardEffect {
}
