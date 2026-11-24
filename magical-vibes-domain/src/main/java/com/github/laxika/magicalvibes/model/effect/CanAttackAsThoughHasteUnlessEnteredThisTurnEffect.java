package com.github.laxika.magicalvibes.model.effect;

/**
 * Static permission: this permanent can attack as though it had haste, unless {@code unlessEnteredThisTurn} and it
 * entered the battlefield this turn (Chaos Lord, the no-argument form). Only the summoning-sickness restriction on
 * attacking is lifted — unlike {@code GrantKeywordEffect(HASTE, …)} it does not allow {@code {T}} ability activation.
 * Wrapped in an {@link AttachedPermanentRestrictionEffect} as {@code (false)} it is the Aura's rule for its creature:
 * "enchanted creature can attack as though it had haste" (Instill Energy). Read in
 * {@code AttackLegalityService.canAttack}.
 *
 * @param unlessEnteredThisTurn whether the permission is off during the turn the permanent entered the battlefield
 */
public record CanAttackAsThoughHasteUnlessEnteredThisTurnEffect(boolean unlessEnteredThisTurn) implements CardEffect {

    /** "Can attack as though it had haste unless it entered this turn." */
    public CanAttackAsThoughHasteUnlessEnteredThisTurnEffect() {
        this(true);
    }
}
