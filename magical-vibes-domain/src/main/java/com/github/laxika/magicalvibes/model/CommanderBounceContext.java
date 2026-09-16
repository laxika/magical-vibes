package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** Last known information for destination-specific triggers after a commander bounce choice. */
public record CommanderBounceContext(Permanent permanent, UUID controllerId, UUID ownerId, boolean wasCreature) {}
