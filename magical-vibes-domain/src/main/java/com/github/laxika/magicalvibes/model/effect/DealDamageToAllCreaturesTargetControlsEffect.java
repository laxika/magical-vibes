package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals a fixed amount of damage to each creature the targeted player controls.
 * Non-targeting — piggybacks on a companion targeting effect (e.g. DealDamageToPlayersEffect TARGET_PLAYER)
 * that provides the targetId (player) on the same stack entry, or on an explicit {@code target(...)}
 * declaration on the card.
 * Used by Radiating Lightning.
 *
 * @param damage amount dealt to each of the target player's creatures
 * @param damagedCreaturesMustAttackThisTurn when true, each creature actually dealt damage this way
 *        must attack this turn if able (transient flag, cleared at end of turn). Creatures whose
 *        damage was fully prevented or redirected are untouched, which is what "each creature dealt
 *        damage this way" requires (Aggravate)
 */
public record DealDamageToAllCreaturesTargetControlsEffect(int damage,
                                                           boolean damagedCreaturesMustAttackThisTurn)
        implements CardEffect {

    public DealDamageToAllCreaturesTargetControlsEffect(int damage) {
        this(damage, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, true, null, false, 1);
    }
}
