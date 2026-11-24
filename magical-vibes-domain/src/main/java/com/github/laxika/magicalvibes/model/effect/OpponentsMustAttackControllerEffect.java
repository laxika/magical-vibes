package com.github.laxika.magicalvibes.model.effect;

/**
 * Static requirement on each opponent's declaration of attackers (CR 508.1d): "each opponent must attack you or a
 * planeswalker you control with at least one creature each combat if able" (Trove of Temptation), or with
 * {@code anyDefender} "each opponent must attack with at least one creature each combat if able", whatever it attacks
 * (Seeker of Slaanesh). The requirement doesn't force every creature to attack, and no attack cost need be paid to
 * obey it.
 *
 * @param anyDefender whether an attack on any player, planeswalker or battle obeys it, rather than only an attack on
 *                    this permanent's controller or a planeswalker they control
 */
public record OpponentsMustAttackControllerEffect(boolean anyDefender) implements CardEffect {

    /** "Each opponent must attack you or a planeswalker you control with at least one creature each combat if able." */
    public OpponentsMustAttackControllerEffect() {
        this(false);
    }

    /** "Each opponent must attack with at least one creature each combat if able." */
    public static OpponentsMustAttackControllerEffect attackingAnyone() {
        return new OpponentsMustAttackControllerEffect(true);
    }
}
