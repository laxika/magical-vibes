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
import com.github.laxika.magicalvibes.model.effect.HeistTargetOpponentLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Resolves the digital Heist keyword action without treating it as a library search. */
@Component
@RequiredArgsConstructor
public class HeistTargetOpponentLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return HeistTargetOpponentLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        if (controllerId == null || targetPlayerId == null) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        List<Card> candidates = new ArrayList<>(deck.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .toList());
        if (candidates.isEmpty()) {
            return;
        }

        Collections.shuffle(candidates);
        candidates = new ArrayList<>(candidates.subList(0, Math.min(3, candidates.size())));

        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String prompt = "Choose one of three random nonland cards from " + targetName
                + "'s library to exile face down and cast with mana of any type.";
        LibrarySearchParams params = LibrarySearchParams.builder(controllerId, candidates)
                .targetPlayerId(targetPlayerId)
                .remainingCount(1)
                .canFailToFind(false)
                .shuffleAfterSelection(false)
                .prompt(prompt)
                .destination(LibrarySearchDestination.HEIST)
                .build();

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.LibrarySearch(params, prompt, false));
        gameLogService.append(gameData,
                GameLog.text(gameData.playerIdToName.get(controllerId) + " heists " + targetName + "'s library."));
    }
}
