package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** A physical card already selected for entry into a child, awaiting removal from its ancestor. */
public record PendingAncestorTransfer(UUID sourceGameId, UUID cardId) { }
