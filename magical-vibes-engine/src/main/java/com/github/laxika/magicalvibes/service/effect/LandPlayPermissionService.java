package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPlaysAdditionalLandEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectConditionResolver;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the current land-play allowance, including conditional static permissions. */
@Component
public class LandPlayPermissionService {

    private final StaticEffectConditionResolver staticEffectConditionResolver;

    public LandPlayPermissionService(StaticEffectConditionResolver staticEffectConditionResolver) {
        this.staticEffectConditionResolver = staticEffectConditionResolver;
    }

    /**
     * Returns the current maximum number of land plays for {@code playerId}. Conditional static
     * permissions are evaluated against the live source permanent so they turn on and off as the
     * battlefield changes.
     */
    public int getMaxLandsThisTurn(GameData gameData, UUID playerId) {
        long conditionalExtra = 0;
        for (UUID controllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                    if (!(effect instanceof ConditionalEffect)) continue;
                    CardEffect activeEffect = staticEffectConditionResolver.resolve(
                            gameData, permanent, controllerId, effect);
                    if (activeEffect instanceof EachPlayerPlaysAdditionalLandEffect) {
                        conditionalExtra = Math.min(Integer.MAX_VALUE, conditionalExtra + 1);
                    } else if (activeEffect instanceof PlaysAdditionalLandEachTurnEffect additional
                            && controllerId.equals(playerId)) {
                        conditionalExtra = Math.min(Integer.MAX_VALUE,
                                conditionalExtra + additional.amount());
                    }
                }
            }
        }
        long total = (long) gameData.getMaxLandsThisTurn(playerId) + conditionalExtra;
        return (int) Math.min(Integer.MAX_VALUE, total);
    }
}
