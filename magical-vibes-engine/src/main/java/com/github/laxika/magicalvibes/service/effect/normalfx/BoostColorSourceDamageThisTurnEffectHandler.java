package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.BoostColorSourceDamageThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostColorSourceDamageThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostColorSourceDamageThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BoostColorSourceDamageThisTurnEffect) effect;

        UUID controllerId = entry.getControllerId();
        gameData.colorSourceDamageBonusThisTurn
                .computeIfAbsent(controllerId, k -> new ConcurrentHashMap<>())
                .merge(e.color(), e.bonus(), Integer::sum);
        String playerName = gameData.playerIdToName.get(controllerId);
        String colorName = e.color().name().toLowerCase();
        String logEntry = playerName + "'s " + colorName + " sources deal +" + e.bonus()
                + " damage this turn (" + entry.getCard().getName() + ").";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} gains +{} {} source damage bonus this turn ({})",
                gameData.id, playerName, e.bonus(), colorName, entry.getCard().getName());
    
    }
}
