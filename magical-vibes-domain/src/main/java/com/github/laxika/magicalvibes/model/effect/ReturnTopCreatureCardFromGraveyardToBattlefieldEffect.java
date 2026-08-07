package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts the top creature card of a graveyard — the creature card closest to the top, i.e. the most
 * recently put there — onto the battlefield under the effect controller's control. Resolves as a
 * no-op when that graveyard holds no creature card. Does not target.
 * <p>
 * {@code graveyard} selects whose graveyard is searched. When
 * {@code assignNoCombatDamageIfReturned} is set and a creature card was actually put onto the
 * battlefield, the stack entry's source permanent additionally assigns no combat damage this turn —
 * the "if you do" half of Bone Dancer, which must not apply when the graveyard held no creature
 * card.
 * <p>
 * Used by Mistmoon Griffin ({@code CONTROLLER}, no damage clause) and Bone Dancer
 * ({@code TARGET_PLAYER} — the defending player — with the damage clause).
 */
public record ReturnTopCreatureCardFromGraveyardToBattlefieldEffect(
        GraveyardOwner graveyard,
        boolean assignNoCombatDamageIfReturned) implements CardEffect {
}
