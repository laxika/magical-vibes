package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;
import java.util.UUID;

/**
 * A resolved effect's permission to play cards its player owns from outside the game (CR 611.2a): Wish's "you may play
 * a card you own from outside the game this turn", Frontier Explorer's basic Plains, or the cast a resolving effect
 * offers from there (Your Wish Is My Command). It is a floating effect on that player, until end of turn, used up as a
 * card it names is played or leaves outside the game.
 *
 * @param cardIds             the cards it permits
 * @param singlePlay          only one of {@link #cardIds} may be played: playing one ends the permission for all
 * @param additionalModalMode a modal card cast with it may choose one more mode than it says
 */
public record OutsideGamePlayPermissionEffect(Set<UUID> cardIds, boolean singlePlay, boolean additionalModalMode)
        implements CardEffect {

    public OutsideGamePlayPermissionEffect {
        cardIds = Set.copyOf(cardIds);
    }
}
