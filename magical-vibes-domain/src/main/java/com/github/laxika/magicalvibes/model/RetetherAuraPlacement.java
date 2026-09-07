package com.github.laxika.magicalvibes.model;

import java.util.UUID;

public record RetetherAuraPlacement(UUID controllerId, UUID graveyardOwnerId, Card auraCard,
                                    UUID attachmentTargetId) {
}
