package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;

/**
 * "...then this creature loses this ability." Placed in the effect list of the printed activated ability that is lost
 * (written with no arguments). As it resolves it makes an ability-removing effect with no stated duration on the
 * source permanent (CR 611.2a): this record again, naming the lost ability object in {@code lostAbility}. The
 * ability can no longer be activated and no longer counts as one of the permanent's abilities. The effect is on that
 * permanent, so a card that leaves and re-enters the battlefield has the ability again (CR 400.7), and the abilities
 * of another card the permanent comes to show are other objects and stay.
 *
 * @param lostAbility the lost ability, or {@code null} in the printed ability's effect list
 */
public record LoseThisActivatedAbilityEffect(ActivatedAbility lostAbility) implements CardEffect {

    /** The effect as printed in the ability it removes. */
    public LoseThisActivatedAbilityEffect() {
        this(null);
    }
}
