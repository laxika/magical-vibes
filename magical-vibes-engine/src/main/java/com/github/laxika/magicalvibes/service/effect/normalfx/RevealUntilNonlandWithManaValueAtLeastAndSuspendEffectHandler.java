package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandWithManaValueAtLeastAndSuspendEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Resolves Sibylline Soothsayer's Temporal Foresight ability. */
@Component
@RequiredArgsConstructor
public class RevealUntilNonlandWithManaValueAtLeastAndSuspendEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilNonlandWithManaValueAtLeastAndSuspendEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var revealEffect = (RevealUntilNonlandWithManaValueAtLeastAndSuspendEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> revealedCards = new ArrayList<>();
        Card qualifyingCard = null;
        while (!library.isEmpty()) {
            Card card = library.removeFirst();
            revealedCards.add(card);
            if (!card.hasType(CardType.LAND)
                    && card.getManaValue() >= revealEffect.minimumManaValue()) {
                qualifyingCard = card;
                break;
            }
        }

        String revealedNames = revealedCards.stream()
                .map(Card::getName)
                .collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " reveals " + revealedNames
                        + " from the top of their library."));

        if (qualifyingCard != null) {
            revealedCards.remove(qualifyingCard);
            exileService.exileCard(gameData, controllerId, qualifyingCard);
            gameData.exiledCardTimeCounters.put(
                    qualifyingCard.getId(), revealEffect.timeCounters());
            gameLogService.append(gameData, GameLog.cardThen(qualifyingCard,
                    " is exiled with " + revealEffect.timeCounters()
                            + " time counters and gains suspend."));
        }

        if (!revealedCards.isEmpty()) {
            Collections.shuffle(revealedCards);
            library.addAll(revealedCards);
        }
    }
}
