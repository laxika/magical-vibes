package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardSharingCardTypeWithDiscardedCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Diviner of Fates' type-based Seek action. */
@Component
@RequiredArgsConstructor
public class SeekCardSharingCardTypeWithDiscardedCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekCardSharingCardTypeWithDiscardedCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(playerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        SeekCardSharingCardTypeWithDiscardedCardsEffect seek =
                (SeekCardSharingCardTypeWithDiscardedCardsEffect) effect;
        List<Card> matching = new ArrayList<>();
        for (Card card : library) {
            if (seek.discardedCards().stream()
                    .anyMatch(discarded -> sharesCardType(card, discarded, gameData, playerId))) {
                matching.add(card);
            }
        }
        if (matching.isEmpty()) {
            return;
        }

        Card sought = matching.get(ThreadLocalRandom.current().nextInt(matching.size()));
        library.removeIf(card -> card.getId().equals(sought.getId()));
        gameData.addCardToHand(playerId, sought);
        triggerCollectionService.checkSeekTriggers(gameData, playerId, List.of(sought));
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text("seeks a card into their hand.").build());
    }

    private boolean sharesCardType(Card first, Card second, GameData gameData, UUID playerId) {
        for (CardType type : CardType.values()) {
            if (gameQueryService.cardHasType(first, type, gameData, playerId)
                    && gameQueryService.cardHasType(second, type, gameData, playerId)) {
                return true;
            }
        }
        return false;
    }
}
