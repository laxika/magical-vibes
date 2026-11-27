package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/**
 * Progress state for Eumidian Wastewaker's attack trigger: the players still to choose, the permanents chosen to be
 * sacrificed (the discards wait in {@link GameData#deferredDiscardSelections}), and, once everything is done
 * together, the cards of the sacrificed permanents and the land cards counted so far.
 */
public class EumidianWastewakerState {

    public boolean active;
    public UUID currentPlayerId;
    public final Deque<UUID> remaining = new ArrayDeque<>();
    public String chosenMode;
    /** The permanents chosen to be sacrificed, in the order they were chosen. */
    public final List<UUID> chosenSacrificeIds = new ArrayList<>();
    /** Whether the chosen permanents' sacrifice has begun (the discards are done by then). */
    public boolean sacrificing;
    /** The cards of the permanents being sacrificed, looked for in graveyards once they are gone. */
    public final List<UUID> sacrificedCardIds = new ArrayList<>();
    public int landCardsPutIntoGraveyard;

    public void reset() {
        active = false;
        currentPlayerId = null;
        remaining.clear();
        chosenMode = null;
        chosenSacrificeIds.clear();
        sacrificing = false;
        sacrificedCardIds.clear();
        landCardsPutIntoGraveyard = 0;
    }
}
