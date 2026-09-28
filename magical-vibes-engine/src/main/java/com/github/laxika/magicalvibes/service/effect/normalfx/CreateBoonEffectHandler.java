package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Boon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateBoonEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Stores a finite-use boon under its controller's player state. */
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
        gameData.boons.add(new Boon(entry.getControllerId(), entry.getCard(),
                boonEffect.triggeredEffect(), boonEffect.uses()));
        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData, GameLog.text(
                playerName + " gets a " + boonEffect.uses() + "-time boon."));
        log.info("Game {} - {} gets a {}-time boon", gameData.id, playerName, boonEffect.uses());
    }
}
