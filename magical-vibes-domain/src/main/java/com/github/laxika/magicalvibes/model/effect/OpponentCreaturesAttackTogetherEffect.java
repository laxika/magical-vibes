package com.github.laxika.magicalvibes.model.effect;

/**
 * Static conditional attack requirement: when one of an opponent's creatures attacks, all
 * creatures that opponent controls must attack if able.
 *
 * <p>This depends on the declared attacker set rather than on static board state, so it is
 * validated during attacker declaration.</p>
 */
public record OpponentCreaturesAttackTogetherEffect() implements CardEffect {
}
