package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealDrawnCardAndRemoveLoyaltyCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RevealDrawnCardAndRemoveLoyaltyCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealDrawnCardAndRemoveLoyaltyCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> drawnCardIds = entry.getDrawnCardIdsThisResolution();
        if (drawnCardIds.isEmpty()) {
            return;
        }

        UUID drawnCardId = drawnCardIds.getLast();
        Card drawnCard = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of()).stream()
                .filter(card -> drawnCardId.equals(card.getId()))
                .findFirst()
                .orElse(null);
        if (drawnCard == null) {
            return;
        }

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData, GameLog.textCardText(playerName + " reveals ", drawnCard, "."));

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null) {
            permanentCounterSupport.removeCounterFromPermanent(
                    gameData, source, CounterType.LOYALTY, drawnCard.getManaValue());
        }
    }
}
