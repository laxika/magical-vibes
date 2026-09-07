package com.github.laxika.magicalvibes.model.effect;

/**
 * For each player, the resolving ability's controller chooses up to one nonbasic land that player
 * controls. The chosen lands are destroyed together, and each land actually destroyed gives its
 * controller an optional search for a basic land onto the battlefield tapped.
 */
public record DestroyUpToOneNonbasicLandPerPlayerThenSearchEffect() implements CardEffect {
}
