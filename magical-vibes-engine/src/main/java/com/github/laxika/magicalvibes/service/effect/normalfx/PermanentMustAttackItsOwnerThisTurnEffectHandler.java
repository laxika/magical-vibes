package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentMustAttackItsOwnerThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves the owner-directed attack rider used by My Crushing Masterstroke. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermanentMustAttackItsOwnerThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PermanentMustAttackItsOwnerThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (permanent == null || !gameQueryService.isCreature(gameData, permanent)) {
            return;
        }

        UUID ownerId = permanent.getCard().getOwnerId();
        if (ownerId == null) {
            ownerId = gameData.defaultControllerOf(permanent.getId());
        }
        if (ownerId == null) {
            return;
        }

        permanent.setMustAttackThisTurn(true);
        permanent.setMustAttackTargetId(ownerId);
        String ownerName = gameData.playerIdToName.get(ownerId);
        gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(),
                " must attack its owner" + (ownerName == null ? "" : " " + ownerName)
                        + " this turn if able."));
        log.info("Game {} - {} must attack its owner {} this turn if able",
                gameData.id, permanent.getCard().getName(), ownerName);
    }
}
