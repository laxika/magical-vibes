package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * A part of a card's text that reads the order of a graveyard (CR 404.2): "the top card of your graveyard", "the top
 * creature card of your graveyard", "the bottom card of target player's graveyard". The engine asks a player to
 * arrange the cards put into their graveyard at the same time (CR 404.3) only in a game where a card with such a
 * part can appear, so every record that reads graveyard order implements this.
 */
public interface GraveyardOrderReader {

    /**
     * The type of the cards whose order among themselves it reads ("the top creature card of your graveyard", with
     * cards of other types skipped), or {@code null} when it reads the whole order (a nonmatching top card counts).
     */
    default CardType graveyardOrderType() {
        return null;
    }
}
