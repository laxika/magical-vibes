package com.github.laxika.magicalvibes.networking.message;

import java.util.List;
import java.util.UUID;

public record ActivateHandAbilityRequest(int handCardIndex, Integer abilityIndex, UUID targetId, Integer xValue,
                                         List<UUID> graveyardCardIds) {
}
