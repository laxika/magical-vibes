package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * The departure a life loss replacement ("if you can't, sacrifice it": Lich's Duel Mastery) would perform instead of
 * the life loss damage to {@code playerId} causes, asked about before the damage is dealt (CR 616.1f: the sacrifice's
 * own replacements are chosen before the modified event happens).
 *
 * @param playerId  the player the damage is dealt to
 * @param intent    the sacrifice asked about; the answered batch waits in {@link GameData#pendingDepartureBatch} until
 *                  the life loss performs it
 * @param suspended whether the damage event waits in {@link GameData#suspendedReplacementEvent} for the answers; once
 *                  they are in, the event goes on and only the answered batch waits for the life loss
 */
public record AskedLifeLossDeparture(UUID playerId, DepartureIntent intent, boolean suspended) {

    /** This departure once its damage event has gone on. */
    public AskedLifeLossDeparture resumed() {
        return new AskedLifeLossDeparture(playerId, intent, false);
    }
}
