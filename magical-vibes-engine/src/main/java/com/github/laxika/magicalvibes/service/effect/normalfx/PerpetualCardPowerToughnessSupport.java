package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BuffTargetCreatureIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;

import java.util.UUID;

/** Stores and applies perpetual P/T changes without mutating frozen card objects. */
public final class PerpetualCardPowerToughnessSupport {

    private PerpetualCardPowerToughnessSupport() {
    }

    public static void remember(GameData gameData, Card card, int powerBoost, int toughnessBoost) {
        if (card == null) {
            return;
        }
        gameData.perpetualCardPowerToughnessModifiers.merge(
                card.getId(),
                new CardPowerToughnessModifier(powerBoost, toughnessBoost),
                (current, added) -> current.add(added.power(), added.toughness()));
    }

    public static void applyToPermanent(GameData gameData, UUID controllerId, Permanent permanent,
                                        int powerBoost, int toughnessBoost) {
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), permanent.getCard().getName(), null, controllerId,
                new BuffTargetCreatureIndefinitelyEffect(powerBoost, toughnessBoost),
                permanent.getId(), null, null, EffectDuration.PERMANENT, 0));
    }

    public static void applyStored(GameData gameData, UUID controllerId, Permanent permanent) {
        UUID cardId = permanent.getCard().getId();
        CardPowerToughnessModifier modifier = gameData.perpetualCardPowerToughnessModifiers.get(cardId);
        if (modifier != null) {
            applyToPermanent(gameData, controllerId, permanent, modifier.power(), modifier.toughness());
        }
    }
}
