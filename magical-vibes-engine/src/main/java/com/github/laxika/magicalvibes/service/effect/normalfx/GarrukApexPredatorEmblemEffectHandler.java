package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostAttackingCreatureOnAttacksYouEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GarrukApexPredatorEmblemEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves Garruk, Apex Predator's −8: the emblem is created under the <em>target opponent's</em>
 * control, so the drawback applies to that player's own attackers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GarrukApexPredatorEmblemEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GarrukApexPredatorEmblemEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (!gameData.playerIds.contains(targetPlayerId)) {
            return;
        }
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        gameData.emblems.add(new Emblem(targetPlayerId, List.of(
                new BoostAttackingCreatureOnAttacksYouEffect(5, 5, Set.of(Keyword.TRAMPLE))
        ), entry.getCard()));

        gameLogService.append(gameData, GameLog.text(playerName + " gets an emblem with \"Whenever a "
                + "creature attacks you, it gets +5/+5 and gains trample until end of turn.\""));

        log.info("Game {} - {} gets Garruk, Apex Predator emblem", gameData.id, playerName);
    }
}
