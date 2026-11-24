package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectYourDamageToTargetCreatureThisTurnEffect.RedirectedDamage;

import java.util.UUID;

/**
 * The redirection {@code RedirectYourDamageToTargetCreatureThisTurnEffect} begins: damage that would be dealt to the
 * protected player and / or the permanents they control ({@code redirected}) is dealt to one fixed permanent instead,
 * for as long as its floating effect lasts, any amount. It may cover combat damage only, or the damage of one source
 * only. It does nothing while the destination is off the battlefield (CR 614.9) or is no longer what the text names:
 * a creature, or for "the chosen permanent" of a creature or planeswalker choice, a creature, planeswalker or battle
 * (CR 614.9). Damage dealt to the destination itself is left alone.
 *
 * @param protectedPlayerId         the player whose damage (or whose permanents' damage) is redirected
 * @param redirectTargetCreatureId  the permanent the damage is dealt to instead
 * @param combatOnly                whether only combat damage is covered
 * @param redirected                whose damage is covered
 * @param damageSourceId            the one source permanent whose damage is covered, or {@code null} for any source
 * @param allowsPlaneswalker        whether the destination may be a planeswalker (or battle) rather than a creature
 */
public record TurnDamageRedirectToCreatureShield(UUID protectedPlayerId,
                                                 UUID redirectTargetCreatureId,
                                                 boolean combatOnly,
                                                 RedirectedDamage redirected,
                                                 UUID damageSourceId,
                                                 boolean allowsPlaneswalker) implements CardEffect {
}
