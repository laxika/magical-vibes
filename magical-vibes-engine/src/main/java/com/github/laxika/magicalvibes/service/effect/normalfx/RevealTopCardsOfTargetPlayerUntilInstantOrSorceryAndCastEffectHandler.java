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
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Chaos Wand's targeted library reveal and free-cast effect. */
@Component
@RequiredArgsConstructor
public class RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        List<Card> deck = targetPlayerId == null ? null : gameData.playerDecks.get(targetPlayerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        List<Card> exiled = new ArrayList<>();
        Card hit = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            exiled.add(card);
            gameLogService.append(gameData, GameLog.builder()
                    .text(targetName + " exiles ").card(card).text(" from the top of their library.")
                    .build());
            if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)) {
                hit = card;
                break;
            }
        }

        if (hit == null) {
            Collections.shuffle(exiled);
            deck.addAll(exiled);
            return;
        }

        String prompt = "You may cast " + hit.getName() + " without paying its mana cost.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, List.of(hit))
                        .targetPlayerId(targetPlayerId)
                        .reveals(true)
                        .canFailToFind(true)
                        .sourceCards(exiled)
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.CAST_WITHOUT_PAYING)
                        .build(),
                prompt,
                true));
        gameLogService.append(gameData, GameLog.builder()
                .text(controllerName + " may cast ").card(hit)
                .text(" without paying its mana cost.").build());
    }
}
