package com.github.laxika.magicalvibes.networking.message;

import java.util.UUID;

public record ActivateExiledAbilityRequest(UUID exiledCardId, Integer abilityIndex, Integer xValue,
                                           UUID targetId) {
}
