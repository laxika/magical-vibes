package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals a fixed amount of damage to each permanent the targeted player controls within
 * {@code scope} — every creature (Radiating Lightning), or every creature and planeswalker
 * (Chandra, Bold Pyromancer's −7).
 * Non-targeting — piggybacks on a companion targeting effect (e.g. DealDamageToPlayersEffect TARGET_PLAYER)
 * that provides the targetId (player) on the same stack entry, or on an explicit {@code target(...)}
 * declaration on the card.
 *
 * @param damage amount dealt to each of the target player's permanents in scope
 * @param scope which of the target player's permanents are damaged
 * @param damagedCreaturesMustAttackThisTurn when true, each creature actually dealt damage this way
 *        must attack this turn if able (transient flag, cleared at end of turn). Creatures whose
 *        damage was fully prevented or redirected are untouched, which is what "each creature dealt
 *        damage this way" requires (Aggravate)
 */
public record DealDamageToPermanentsTargetControlsEffect(int damage,
                                                         DamagedPermanentScope scope,
                                                         boolean damagedCreaturesMustAttackThisTurn)
        implements CardEffect {

    public DealDamageToPermanentsTargetControlsEffect(int damage) {
        this(damage, DamagedPermanentScope.CREATURES, false);
    }

    public DealDamageToPermanentsTargetControlsEffect(int damage, boolean damagedCreaturesMustAttackThisTurn) {
        this(damage, DamagedPermanentScope.CREATURES, damagedCreaturesMustAttackThisTurn);
    }

    public DealDamageToPermanentsTargetControlsEffect(int damage, DamagedPermanentScope scope) {
        this(damage, scope, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, true, null, false, 1);
    }
}
