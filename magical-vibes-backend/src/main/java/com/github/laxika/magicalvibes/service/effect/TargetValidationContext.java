package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TargetZone;

import java.util.UUID;

public record TargetValidationContext(
        GameData gameData,
        UUID targetPermanentId,
        TargetZone targetZone,
        Card sourceCard
) {}

