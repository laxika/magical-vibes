package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

/**
 * Non-blocking private display of the top cards of a player's library (Orcish Spy). The cards are
 * shown only to the looking player and stay on top in their original order — no selection, no
 * reordering. {@code playerName} is the library's owner.
 */
public record RevealLibraryTopMessage(MessageType type, List<CardView> cards, String playerName) {

    public RevealLibraryTopMessage(List<CardView> cards, String playerName) {
        this(MessageType.REVEAL_LIBRARY_TOP, cards, playerName);
    }
}
