package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CraftyCutpurseEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CraftyCutpurseEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CraftyCutpurseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.playersGatheringTokensThisTurn.add(controllerId);

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(
                "Tokens that would be created under an opponent's control this turn are created under "
                        + playerName + "'s control instead."));
    }
}
