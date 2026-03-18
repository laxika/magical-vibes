package com.github.laxika.magicalvibes.service.exile;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromExileToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Resolves effects that return targeted exiled cards to their owner's hand.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExileReturnResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @HandlesEffect(ReturnTargetCardFromExileToHandEffect.class)
    void resolveReturnTargetCardFromExileToHand(GameData gameData, StackEntry entry, ReturnTargetCardFromExileToHandEffect effect) {
        if (entry.getTargetZone() != Zone.EXILE || entry.getTargetPermanentId() == null) {
            String fizzleLog = entry.getDescription() + " fizzles (no valid exile target).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            return;
        }

        Card targetCard = gameQueryService.findCardInExileById(gameData, entry.getTargetPermanentId());
        String filterLabel = CardPredicateUtils.describeFilter(effect.filter());

        if (targetCard == null) {
            String fizzleLog = entry.getDescription() + " fizzles (target " + filterLabel + " is no longer in exile).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            return;
        }

        if (effect.filter() != null && !gameQueryService.matchesCardPredicate(targetCard, effect.filter(), null)) {
            String fizzleLog = entry.getDescription() + " fizzles (target is not a " + filterLabel + ").";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            return;
        }

        // Remove card from exile
        UUID ownerId = gameQueryService.findExileOwnerById(gameData, entry.getTargetPermanentId());
        if (ownerId != null) {
            List<Card> exile = gameData.playerExiledCards.get(ownerId);
            if (exile != null) {
                exile.removeIf(c -> c.getId().equals(targetCard.getId()));
            }
        }

        // Put into owner's hand
        UUID controllerId = entry.getControllerId();
        gameData.playerHands.get(controllerId).add(targetCard);

        String logMsg = entry.getDescription() + " returns " + targetCard.getName() + " from exile to hand.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
    }
}
