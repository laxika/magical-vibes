package com.github.laxika.magicalvibes.service;

import java.util.UUID;

/**
 * Engine-facing read port for player connectivity.
 *
 * <p>The mutation layer depends on this semantic query rather than on a transport adapter.
 */
public interface PlayerConnectionState {

    boolean isPlayerConnected(UUID playerId);
}
