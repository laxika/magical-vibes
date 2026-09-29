package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfExiledCardIntoTopFiveEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a duplicate conjure for a card exiled by the preceding graveyard effect. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfExiledCardIntoTopFiveEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfExiledCardIntoTopFiveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null && entry.getTargetCardIds() != null
                && !entry.getTargetCardIds().isEmpty()) {
            targetCardId = entry.getTargetCardIds().getFirst();
        }
        Card exiledCard = gameQueryService.findCardInExileById(gameData, targetCardId);
        if (exiledCard == null) {
            return;
        }

        Card duplicate = exiledCard.createRuntimeCopyWithNewId();
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.setToken(true);
        duplicate.setTokenCard(true);
        duplicate.freeze();

        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null) {
            return;
        }
        int insertionBound = Math.min(4, library.size());
        int insertionIndex = ThreadLocalRandom.current().nextInt(insertionBound + 1);
        library.add(insertionIndex, duplicate);
        gameLogService.append(gameData, GameLog.textCardText(
                "A duplicate of ", duplicate, " is conjured into the top five cards of your library."));
    }
}
