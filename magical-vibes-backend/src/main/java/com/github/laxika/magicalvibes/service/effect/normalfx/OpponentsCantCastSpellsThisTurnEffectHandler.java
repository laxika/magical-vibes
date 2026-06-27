package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpponentsCantCastSpellsThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentsCantCastSpellsThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        for (UUID pid : gameData.orderedPlayerIds) {
            if (!pid.equals(controllerId)) {
                gameData.playersSilencedThisTurn.add(pid);
            }
        }

        String logEntry = gameData.playerIdToName.get(controllerId) + "'s opponents can't cast spells this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
    }
}
