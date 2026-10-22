package com.github.laxika.magicalvibes.model.effect;

/**
 * "...then this creature loses this ability." Placed in the effect list of the printed activated
 * ability that is lost: when it resolves, the source permanent permanently loses that ability (it can
 * no longer be activated and no longer counts as one of the permanent's abilities). The loss is tied
 * to the permanent object, so a card that leaves and re-enters the battlefield has the ability again.
 */
public record LoseThisActivatedAbilityEffect() implements CardEffect {
}
