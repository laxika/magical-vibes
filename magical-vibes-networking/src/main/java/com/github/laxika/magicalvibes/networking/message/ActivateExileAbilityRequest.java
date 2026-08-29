package com.github.laxika.magicalvibes.networking.message;

import java.util.UUID;

public record ActivateExileAbilityRequest(UUID cardId, Integer abilityIndex, Integer xValue, UUID targetId) {
}
