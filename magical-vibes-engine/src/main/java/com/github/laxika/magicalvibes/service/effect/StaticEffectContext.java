package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.UUID;

/**
 * Inputs shared by static-effect handlers. The source controller is explicit because an entering
 * permanent can be evaluated before battlefield membership makes its controller discoverable.
 */
public record StaticEffectContext(Permanent source, Permanent target, UUID sourceControllerId,
                                  boolean targetOnSameBattlefield, GameData gameData) {
}

