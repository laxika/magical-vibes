package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowCastMatchingCardsFromAnyGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllowCastMatchingCardsFromAnyGraveyardThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowCastMatchingCardsFromAnyGraveyardThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AllowCastMatchingCardsFromAnyGraveyardThisTurnEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.graveyardCastFilterPermissionsThisTurn.add(
                new GameData.GraveyardCastFilterPermission(controllerId, e.filter(), true, true));

        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + " may cast matching spells from any graveyard this turn. They are exiled instead."));
        log.info("Game {} - {} may cast spells matching {} from any graveyard this turn",
                gameData.id, gameData.playerIdToName.get(controllerId), e.filter());
    }
}
