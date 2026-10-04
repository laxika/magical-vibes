package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowCastMatchingCardsFromDamagedPlayerGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a combat-damage trigger that grants access to the damaged player's graveyard. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AllowCastMatchingCardsFromDamagedPlayerGraveyardThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowCastMatchingCardsFromDamagedPlayerGraveyardThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (AllowCastMatchingCardsFromDamagedPlayerGraveyardThisTurnEffect) effect;
        UUID damagedPlayerId = entry.getTargetId();
        if (damagedPlayerId == null || !gameData.playerIds.contains(damagedPlayerId)) {
            return;
        }

        gameData.graveyardCastFilterPermissionsThisTurn.add(
                new GameData.GraveyardCastFilterPermission(
                        entry.getControllerId(), grant.filter(), grant.singleUse(), damagedPlayerId,
                        grant.anyManaType()));

        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                + " may cast a matching spell from "
                + gameData.playerIdToName.get(damagedPlayerId) + "'s graveyard this turn."));
        log.info("Game {} - {} may cast cards matching {} from {}'s graveyard this turn",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()), grant.filter(),
                gameData.playerIdToName.get(damagedPlayerId));
    }
}
