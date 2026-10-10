package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameRevealTopCardEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseCardNameRevealTopCardEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardNameRevealTopCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choiceContext = new ChoiceContext.ChooseCardNameRevealTopCardChoice(entry.getControllerId());
        // Suggestions come from public zones plus the chooser's own hidden hand and library, so they
        // never reveal an opponent's hidden cards.
        TreeSet<String> nameSet = new TreeSet<>(libraryRevealSupport.collectPublicCardNames(gameData));
        UUID controllerId = entry.getControllerId();
        for (Card card : gameData.playerHands.getOrDefault(controllerId, List.of())) {
            nameSet.add(card.getName());
        }
        for (Card card : gameData.playerDecks.getOrDefault(controllerId, List.of())) {
            nameSet.add(card.getName());
        }
        List<String> cardNames = new ArrayList<>(nameSet);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                entry.getControllerId(), null, null, choiceContext, cardNames, "Choose a card name."));

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        log.info("Game {} - Awaiting {} to choose a card name ({})",
                gameData.id, playerName, entry.getCard().getName());
    }
}
