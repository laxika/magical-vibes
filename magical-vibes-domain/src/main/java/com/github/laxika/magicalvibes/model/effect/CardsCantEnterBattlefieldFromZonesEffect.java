package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Set;

/**
 * Static effect: cards matching {@code filter} can't enter the battlefield from any zone listed in
 * {@code zones}. This is a continuous restriction that stops reanimation (return a matching card
 * from a graveyard to the battlefield), undying/persist returns, and effects that put matching
 * cards from libraries onto the battlefield. The blocked card stays in its current zone.
 * <p>
 * A {@code null} filter matches every card. Does not affect tokens (which are not cards).
 * <p>
 * {@link Zone#GRAVEYARD} and {@link Zone#LIBRARY} are checked by the put and return sites before the card
 * leaves its zone; {@link Zone#EXILE} is checked by {@code BattlefieldPlacementService} as a card arriving
 * from exile is placed, which leaves it in exile. {@link Zone#BATTLEFIELD} and {@link Zone#STACK} are not
 * enforced.
 * <p>
 * Used by Grafdigger's Cage (DKA), which supplies a {@code CardTypePredicate(CREATURE)} filter and
 * {@code Set.of(GRAVEYARD, LIBRARY)} so only creature cards entering from a graveyard or library are
 * blocked. Floated on a player by {@link GrantStaticEffectToPlayerEffect} it is a rule change for its
 * duration that binds every card (Bad Wolf Bay's "cards can't enter from exile this turn", CR 611.2c).
 */
public record CardsCantEnterBattlefieldFromZonesEffect(CardPredicate filter, Set<Zone> zones) implements CardEffect {
}
