package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * Carries the common per-effect match data passed to every trigger collector handler.
 *
 * @param gameData     the current game state
 * @param permanent    the permanent whose effect slot produced the trigger
 * @param controllerId the controller of that permanent
 * @param rawEffect    the effect presented to the collector (some dispatch paths remove an outer
 *                     once-per-turn wrapper); it may be {@code MayEffect}-wrapped
 */
public record TriggerMatchContext(
        GameData gameData,
        Permanent permanent,
        UUID controllerId,
        CardEffect rawEffect
) {}
