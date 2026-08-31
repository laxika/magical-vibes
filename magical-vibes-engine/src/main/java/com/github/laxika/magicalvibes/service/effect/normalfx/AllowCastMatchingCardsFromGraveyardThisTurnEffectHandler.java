package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowCastMatchingCardsFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllowCastMatchingCardsFromGraveyardThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowCastMatchingCardsFromGraveyardThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AllowCastMatchingCardsFromGraveyardThisTurnEffect) effect;
        UUID controllerId = entry.getControllerId();

        // Recorded as a filter, not per card: cards that reach the graveyard later this turn are
        // covered too. Turn cleanup drops the grant.
        gameData.graveyardCastFilterPermissionsThisTurn.add(
                new GameData.GraveyardCastFilterPermission(controllerId, e.filter(), e.singleUse(),
                        e.entryCounterType(), e.grantedSubtype(), false, false,
                        e.additionalCost(), e.enterWithCounter(), e.enterWithCounterCount()));

        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + " may cast matching spells from their graveyard this turn."));
        log.info("Game {} - {} may cast spells matching {} from their graveyard this turn",
                gameData.id, gameData.playerIdToName.get(controllerId), e.filter());
    }
}
