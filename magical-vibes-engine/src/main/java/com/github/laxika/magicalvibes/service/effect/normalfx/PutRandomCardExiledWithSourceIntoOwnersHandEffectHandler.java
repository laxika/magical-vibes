package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutRandomCardExiledWithSourceIntoOwnersHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Returns one randomly selected card tracked as exiled with the source permanent to its owner's
 * hand.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutRandomCardExiledWithSourceIntoOwnersHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutRandomCardExiledWithSourceIntoOwnersHandEffect.class;
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
        List<ExiledCardEntry> matching = gameData.exiledCards.stream()
                .filter(exiled -> sourceId.equals(exiled.sourcePermanentId()))
                .toList();
        if (matching.isEmpty()) {
            return;
        }

        ExiledCardEntry chosen = matching.get(ThreadLocalRandom.current().nextInt(matching.size()));
        if (!gameData.removeFromExile(chosen.card().getId())) {
            return;
        }

        UUID ownerId = chosen.ownerId();
        gameData.addCardToHand(ownerId, chosen.card());
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(ownerId) + " puts ", chosen.card(),
                " from exile into their hand."));
        log.info("Game {} - {} returns {} at random from exile to its owner's hand via {}",
                gameData.id, gameData.playerIdToName.get(ownerId), chosen.card().getName(),
                entry.getCard().getName());
    }
}
