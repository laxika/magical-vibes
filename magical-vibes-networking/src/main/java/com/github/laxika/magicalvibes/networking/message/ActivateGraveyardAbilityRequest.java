package com.github.laxika.magicalvibes.networking.message;

import java.util.UUID;

public record ActivateGraveyardAbilityRequest(int graveyardCardIndex, Integer abilityIndex, Integer xValue,
                                              UUID targetId) {
}
