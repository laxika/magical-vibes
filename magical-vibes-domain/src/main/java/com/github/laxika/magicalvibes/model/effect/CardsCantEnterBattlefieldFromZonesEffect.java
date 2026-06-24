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
 * <b>Caveat:</b> only {@link Zone#GRAVEYARD} and {@link Zone#LIBRARY} are actually enforced, because
 * those are the only zones with battlefield-entry gating sites wired up. Passing other zones
 * ({@link Zone#EXILE}, {@link Zone#BATTLEFIELD}, {@link Zone#STACK}) silently does nothing.
 * <p>
 * Used by Grafdigger's Cage (DKA), which supplies a {@code CardTypePredicate(CREATURE)} filter and
 * {@code Set.of(GRAVEYARD, LIBRARY)} so only creature cards entering from a graveyard or library are
 * blocked.
 */
public record CardsCantEnterBattlefieldFromZonesEffect(CardPredicate filter, Set<Zone> zones) implements CardEffect {
}
