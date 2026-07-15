package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwardRestrictedManaEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardRestrictedManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AwardRestrictedManaEffect) effect;
        UUID controllerId = entry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(controllerId);
        e.applyTo(pool);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " adds " + e.amount() + " " + e.color().getCode()
                + " (" + e.restriction().description() + ").";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} adds {} {} (restricted: {})", gameData.id, playerName, e.amount(), e.color(), e.restriction().description());
    }
}
