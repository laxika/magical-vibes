package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.AbilityZones;

/**
 * Describes a choice made during the pregame procedure from an opening-hand card (CR 103.6). A card adds it,
 * wrapped in a {@link MayEffect}, as a static ability of the hand: "if this card is in your opening hand" names
 * the zone it functions in (CR 113.6b). {@code MulliganService.startGame} offers it once the mulligans are done.
 */
public interface PregameChoiceEffect extends CardEffect {

    /** Whether the choice is available only to a player who is not starting the game. */
    default boolean onlyForNonStartingPlayer() {
        return false;
    }

    @Override
    default AbilityZones staticAbilityZones() {
        return AbilityZones.HAND;
    }
}
