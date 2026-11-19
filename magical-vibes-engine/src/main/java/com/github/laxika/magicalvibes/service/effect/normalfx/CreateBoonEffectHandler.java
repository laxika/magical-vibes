package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateBoonEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Gives the resolving spell or ability's controller the boon {@link CreateBoonEffect} describes. */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateBoonEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateBoonEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateBoonEffect boonEffect = (CreateBoonEffect) effect;
        TemporaryGlobalTriggeredAbility boon = boonEffect.usedAsItResolves()
                ? TemporaryGlobalTriggeredAbility.boonUsedAsItResolves(entry.getControllerId(), entry.getCard(),
                        boonEffect.ability(), entry.getSourcePermanentId())
                : TemporaryGlobalTriggeredAbility.boon(entry.getControllerId(), entry.getCard(), boonEffect.ability(),
                        entry.getSourcePermanentId(), boonEffect.uses());
        if (boonEffect.targetFilter() != null) {
            boon = boon.withTargetFilter(boonEffect.targetFilter());
        }
        gameData.temporaryGlobalTriggeredAbilities.add(boon);
        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData, GameLog.text(
                playerName + " gets a " + boonEffect.uses() + "-time boon."));
        log.info("Game {} - {} gets a {}-time boon", gameData.id, playerName, boonEffect.uses());
    }
}
