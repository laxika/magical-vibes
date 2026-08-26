package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndAllWithSameNameEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetGraveyardCardAndAllWithSameNameEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetGraveyardCardAndAllWithSameNameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetId());
        if (targetCard == null) {
            return;
        }

        String cardName = targetCard.getName();
        List<Permanent> matchingPermanents = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (permanent.getCard().getName().equals(cardName)) {
                matchingPermanents.add(permanent);
            }
        });

        exileGraveyardCard(gameData, targetCard);

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) {
                continue;
            }
            List<Card> matchingCards = graveyard.stream()
                    .filter(card -> card.getName().equals(cardName))
                    .toList();
            for (Card card : matchingCards) {
                exileGraveyardCard(gameData, card);
            }
        }

        for (Permanent permanent : matchingPermanents) {
            if (permanentRemovalService.removePermanentToExile(gameData, permanent)) {
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
                log.info("Game {} - {} is exiled by {}", gameData.id, permanent.getCard().getName(),
                        entry.getCard().getName());
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private void exileGraveyardCard(GameData gameData, Card card) {
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        if (ownerId == null) {
            return;
        }
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, card.getId());
        exileService.exileCard(gameData, ownerId, card);
        gameLogService.append(gameData, GameLog.cardThen(card, " is exiled."));
    }
}
