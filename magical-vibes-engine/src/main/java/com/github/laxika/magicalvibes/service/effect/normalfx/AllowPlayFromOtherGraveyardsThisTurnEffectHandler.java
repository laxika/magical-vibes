package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowPlayFromOtherGraveyardsThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllowPlayFromOtherGraveyardsThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowPlayFromOtherGraveyardsThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.graveyardPlayFilterPermissionsThisTurn.add(
                new GameData.GraveyardPlayFilterPermission(
                        controllerId, GraveyardSearchScope.OPPONENT_GRAVEYARD, new CardTruePredicate()));

        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + " may play cards from other players' graveyards this turn."));
        log.info("Game {} - {} may play cards from other players' graveyards this turn",
                gameData.id, gameData.playerIdToName.get(controllerId));
    }
}
