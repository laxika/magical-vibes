package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.model.Zone;
import java.util.UUID;

public record ActivatePlanarAbilityRequest(UUID sourceId, int abilityIndex, Integer xValue,
                                          UUID targetId, Zone targetZone) {}
