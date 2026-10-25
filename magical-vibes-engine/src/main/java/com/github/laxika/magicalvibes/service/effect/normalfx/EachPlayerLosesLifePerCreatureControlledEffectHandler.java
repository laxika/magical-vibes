package com.github.laxika.magicalvibes.service.effect.normalfx;

import java.util.UUID;
import java.util.Map;
import java.util.LinkedHashMap;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesLifePerCreatureControlledEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.CreatureCountSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerLosesLifePerCreatureControlledEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerLosesLifePerCreatureControlledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerLosesLifePerCreatureControlledEffect) effect;
        Map<UUID, Integer> losses = new LinkedHashMap<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (e.opponentsOnly() && playerId.equals(entry.getControllerId())) {
                return;
            }
            int creatureCount = 0;
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && (!e.attackingOnly() || permanent.isAttacking())) {
                    creatureCount += CreatureCountSupport.creatureCount(gameData, permanent, gameQueryService);
                }
            }

            int lifeLoss = creatureCount * e.lifePerCreature();
            if (lifeLoss > 0) {
                losses.put(playerId, lifeLoss);
            }
        });
        lifeSupport.applyLifeLosses(gameData, losses, entry.getCard().getName());
    }
}
