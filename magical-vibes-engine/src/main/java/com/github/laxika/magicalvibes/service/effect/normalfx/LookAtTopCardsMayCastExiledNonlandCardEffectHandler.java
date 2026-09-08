package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayCastExiledNonlandCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsMayCastExiledNonlandCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsMayCastExiledNonlandCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardsMayCastExiledNonlandCardEffect typedEffect =
                (LookAtTopCardsMayCastExiledNonlandCardEffect) effect;
        int count = amountEvaluationService.evaluate(gameData, typedEffect.count(),
                AmountContext.forStackEntry(entry, null));
        if (count <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        int actualCount = Math.min(count, deck.size());
        List<Card> topCards = LibraryRevealSupport.takeTopCards(deck, actualCount);
        String controllerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(controllerName + " looks at the top "
                + actualCount + " cards of their library."));

        List<Card> nonlandCards = topCards.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .toList();
        if (nonlandCards.isEmpty()) {
            Collections.shuffle(topCards);
            deck.addAll(topCards);
            return;
        }

        String prompt = "You may exile a nonland card from among them.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, nonlandCards)
                        .reveals(false)
                        .canFailToFind(true)
                        .sourceCards(new ArrayList<>(topCards))
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.EXILE_AND_MAY_CAST_WITHOUT_PAYING)
                        .build(),
                prompt,
                true));
        log.info("Game {} - {} looks at the top {} cards for {}",
                gameData.id, controllerName, actualCount, entry.getCard().getName());
    }
}
