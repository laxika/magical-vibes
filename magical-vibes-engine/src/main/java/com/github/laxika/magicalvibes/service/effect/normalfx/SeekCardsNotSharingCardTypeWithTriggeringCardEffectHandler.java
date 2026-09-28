package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardsNotSharingCardTypeWithTriggeringCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class SeekCardsNotSharingCardTypeWithTriggeringCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekCardsNotSharingCardTypeWithTriggeringCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(playerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        Card triggeringCard = gameQueryService.findCardById(gameData, entry.getTriggeringCardId());
        if (triggeringCard == null) {
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            return;
        }

        SeekCardsNotSharingCardTypeWithTriggeringCardEffect seek =
                (SeekCardsNotSharingCardTypeWithTriggeringCardEffect) effect;
        List<Card> matching = new ArrayList<>();
        for (Card card : library) {
            if (!sharesCardType(card, triggeringCard, gameData, playerId)) {
                matching.add(card);
            }
        }

        List<Card> soughtCards = new ArrayList<>();
        for (int i = 0; i < seek.count() && !matching.isEmpty(); i++) {
            Card sought = matching.remove(ThreadLocalRandom.current().nextInt(matching.size()));
            library.remove(sought);
            gameData.addCardToHand(playerId, sought);
            soughtCards.add(sought);
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, playerId);

        if (soughtCards.isEmpty()) {
            return;
        }
        triggerCollectionService.checkSeekTriggers(gameData, playerId, soughtCards);
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(playerId) + " seeks " + soughtCards.size() + " cards into their hand."));
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
