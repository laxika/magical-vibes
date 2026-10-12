package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DamageBonusGrant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.BoostColorSourceDamageThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostColorSourceDamageThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostColorSourceDamageThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BoostColorSourceDamageThisTurnEffect) effect;

        UUID controllerId = entry.getControllerId();
        GameData.addDamageBonusGrant(gameData.colorSourceDamageBonusThisTurn, controllerId,
                new DamageBonusGrant(entry.getCard().getName(), e.color(), e.bonus()));
        String playerName = gameData.playerIdToName.get(controllerId);
        String colorName = e.color().name().toLowerCase();
        
        gameLogService.append(gameData, GameLog.builder().text(playerName + "'s " + colorName + " sources deal +" + e.bonus() + " damage this turn (").card(entry.getCard()).text(").").build());
        log.info("Game {} - {} gains +{} {} source damage bonus this turn ({})",
                gameData.id, playerName, e.bonus(), colorName, entry.getCard().getName());
    
    }
}
