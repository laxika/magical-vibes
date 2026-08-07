package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/**
 * Static replacement effect (CR 614.1): if a source of one of {@code colors} would deal damage to a
 * player, it deals that much damage plus {@code amount} to that player instead.
 *
 * <p>Source-colour scoped and recipient-agnostic: every player is affected, no matter who controls
 * the damage source or the permanent carrying this effect. Both combat and noncombat damage to a
 * player get the bonus; damage to permanents never does. Contrast
 * {@link AdditionalControllerDamageEffect} (controller's stack sources only, combat excluded).
 *
 * <p>Multiple instances stack additively. Only applies when the source would deal at least 1 damage.
 * Queried by {@code GameQueryService.getDamageToPlayerColorSourceBonus} from the two player-damage
 * entry points ({@code DamageSupport.dealDamageToPlayer} and {@code CombatDamageService}).
 *
 * <p>Example: Tok-Tok, Volcano Born —
 * {@code new AdditionalDamageToPlayersFromColorSourcesEffect(Set.of(CardColor.RED), 1)}.
 */
public record AdditionalDamageToPlayersFromColorSourcesEffect(Set<CardColor> colors, int amount) implements CardEffect {
}
