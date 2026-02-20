package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

public record WarpWorldAuraChoiceRequest(UUID controllerId, Card auraCard, List<UUID> validTargetIds) {
}
