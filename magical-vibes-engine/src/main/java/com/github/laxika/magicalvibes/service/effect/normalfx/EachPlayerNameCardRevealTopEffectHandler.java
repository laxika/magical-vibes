package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerNameCardRevealTopEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerNameCardRevealTopEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerNameCardRevealTopEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();

        // Build APNAP order: active player first, then the rest
        List<UUID> playerOrder = new ArrayList<>();
        playerOrder.add(gameData.activePlayerId);
        for (UUID pid : gameData.orderedPlayerIds) {
            if (!pid.equals(gameData.activePlayerId)) {
                playerOrder.add(pid);
            }
        }

        // Begin the first player's card name choice
        UUID firstPlayerId = playerOrder.getFirst();
        var choiceContext = new ChoiceContext.EachPlayerCardNameRevealChoice(
                playerOrder, new LinkedHashMap<>());

        List<String> cardNames = libraryRevealSupport.collectAllCardNamesInGame(gameData);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                firstPlayerId, null, null, choiceContext, cardNames, "Choose a card name."));

        String playerName = gameData.playerIdToName.get(firstPlayerId);
        log.info("Game {} - Awaiting {} to choose a card name (Conundrum Sphinx)", gameData.id, playerName);
    
    }
}
