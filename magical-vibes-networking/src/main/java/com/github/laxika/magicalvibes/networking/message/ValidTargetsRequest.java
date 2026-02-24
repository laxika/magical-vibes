package com.github.laxika.magicalvibes.networking.message;

import java.util.List;
import java.util.UUID;

public record ValidTargetsRequest(
        Integer cardIndex,
        Integer permanentIndex,
        Integer abilityIndex,
        List<UUID> alreadySelectedIds
) {
}
