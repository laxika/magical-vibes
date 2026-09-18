package com.github.laxika.magicalvibes.service.effect.normalfx;

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
public class EachPlayerLosesLifePerCreatureControlledEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerLosesLifePerCreatureControlledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerLosesLifePerCreatureControlledEffect) effect;
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
                lifeSupport.applyLifeLoss(gameData, playerId, lifeLoss, entry.getCard().getName());
            }
        });
    }
}
