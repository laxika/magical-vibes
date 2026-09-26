package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** Identifies one activation of a game within a session. */
public record GameContext(UUID sessionId, UUID activeGameId, long activationEpoch) { }
