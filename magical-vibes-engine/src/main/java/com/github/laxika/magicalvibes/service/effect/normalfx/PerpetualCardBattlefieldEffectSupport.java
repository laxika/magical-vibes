package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;

import java.util.List;
import java.util.UUID;

/** Stores static effects perpetually granted to a physical card and reapplies them on entry. */
public final class PerpetualCardBattlefieldEffectSupport {

    private PerpetualCardBattlefieldEffectSupport() {
    }

    public static void remember(GameData gameData, Card card, List<CardEffect> effects) {
        if (card == null || effects == null || effects.isEmpty()) {
            return;
        }
        gameData.perpetualCardBattlefieldEffectGrants
                .computeIfAbsent(card.getId(), ignored ->
                        java.util.Collections.synchronizedList(new java.util.ArrayList<>()))
                .addAll(effects);
    }

    public static void applyStored(GameData gameData, UUID controllerId, Permanent permanent) {
        if (permanent == null) {
            return;
        }
        apply(gameData, controllerId, permanent,
                gameData.perpetualCardBattlefieldEffectGrants.get(permanent.getCard().getId()));
    }

    public static void apply(GameData gameData, UUID controllerId, Permanent permanent,
                             List<CardEffect> effects) {
        if (permanent == null || effects == null || effects.isEmpty()) {
            return;
        }
        for (CardEffect effect : effects) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), permanent.getCard().getName(), null, controllerId,
                    effect, permanent.getId(), null, null, EffectDuration.PERMANENT, 0));
        }
    }
}
