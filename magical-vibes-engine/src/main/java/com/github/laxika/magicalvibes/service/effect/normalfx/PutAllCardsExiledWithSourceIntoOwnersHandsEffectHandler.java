package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutAllCardsExiledWithSourceIntoOwnersHandsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutAllCardsExiledWithSourceIntoOwnersHandsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null && entry.getSourcePermanentSnapshot() != null) {
            sourcePermanentId = entry.getSourcePermanentSnapshot().getId();
        }
        if (sourcePermanentId == null) {
            return;
        }

        UUID sourceId = sourcePermanentId;
        List<ExiledCardEntry> toReturn = gameData.exiledCards.stream()
                .filter(exiled -> sourceId.equals(exiled.sourcePermanentId()))
                .toList();

        for (ExiledCardEntry exiled : toReturn) {
            if (!gameData.removeFromExile(exiled.card().getId())) {
                continue;
            }
            UUID ownerId = exiled.ownerId();
            gameData.addCardToHand(ownerId, exiled.card());
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(ownerId) + " puts ", exiled.card(), " from exile into their hand."));
            log.info("Game {} - {} returns {} from exile to its owner's hand via {}",
                    gameData.id, gameData.playerIdToName.get(ownerId), exiled.card().getName(), entry.getCard().getName());
        }
    }
}
