package com.github.laxika.magicalvibes.model.effect;

/**
 * Until end of turn, the controller may cast the top card of their graveyard if it is an instant or
 * sorcery, paying its mana cost. A spell cast this way is exiled instead of being put into a
 * graveyard. A floating effect on the controller that ends at the cleanup step. Used by Bösium Strip.
 */
public record MayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurnEffect() implements CardEffect, GraveyardOrderReader {
}
