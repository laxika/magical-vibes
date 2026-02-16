package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;

public record StaticEffectContext(Permanent source, Permanent target, boolean targetOnSameBattlefield,
                                  GameData gameData) {
}
