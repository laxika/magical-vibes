package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** State and continuation support for Sanar's Vivid reveal. */
@Component
@RequiredArgsConstructor
public class VividRevealSupport {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public void begin(GameData gameData, UUID controllerId, List<Card> revealedCards) {
        Set<CardColor> colors = EnumSet.noneOf(CardColor.class);
        for (var permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            colors.addAll(gameQueryService.getEffectiveColors(gameData, permanent));
        }

        List<CardColor> orderedColors = List.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK,
                CardColor.RED, CardColor.GREEN).stream().filter(colors::contains).toList();
        advance(gameData, controllerId, List.copyOf(revealedCards), orderedColors, 0, List.of(), false);
    }

    public void handleChoice(GameData gameData, PendingInteraction.VividCardChoice choice,
                             List<UUID> cardIds) {
        if (cardIds == null) {
            cardIds = List.of();
        }
        if (cardIds.size() > 1 || cardIds.stream().distinct().count() != cardIds.size()) {
            throw new IllegalStateException("Choose at most one Vivid card");
        }
        if (!choice.validCardIds().containsAll(cardIds)) {
            throw new IllegalStateException("Invalid Vivid card");
        }

        gameData.interaction.clearAwaitingInput();
        List<UUID> selected = new ArrayList<>(choice.selectedCardIds());
        selected.addAll(cardIds);
        advance(gameData, choice.playerId(), choice.revealedCards(), choice.colors(),
                choice.nextColorIndex() + 1, selected, true);
    }

    private void advance(GameData gameData, UUID controllerId, List<Card> revealedCards,
                         List<CardColor> colors, int nextColorIndex, List<UUID> selectedCardIds,
                         boolean resumeWhenFinished) {
        for (int i = nextColorIndex; i < colors.size(); i++) {
            CardColor color = colors.get(i);
            List<UUID> validCardIds = revealedCards.stream()
                    .filter(card -> !selectedCardIds.contains(card.getId()) && hasColor(card, color))
                    .map(Card::getId)
                    .toList();
            if (validCardIds.isEmpty()) {
                nextColorIndex = i + 1;
                continue;
            }

            interactionHandlerRegistry.begin(gameData, new PendingInteraction.VividCardChoice(
                    controllerId, revealedCards, validCardIds, colors, i, selectedCardIds,
                    "You may exile a " + color.name().toLowerCase() + " card for Vivid."));
            return;
        }

        finish(gameData, controllerId, revealedCards, selectedCardIds);
        if (resumeWhenFinished) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }

    private void finish(GameData gameData, UUID controllerId, List<Card> revealedCards,
                        List<UUID> selectedCardIds) {
        List<Card> remaining = new ArrayList<>();
        for (Card card : revealedCards) {
            if (selectedCardIds.contains(card.getId())) {
                exileService.exileCard(gameData, controllerId, card);
                gameData.exilePlayPermissions.put(card.getId(), controllerId);
                gameData.exilePlayPermissionsExpireEndOfTurn.add(card.getId());
            } else {
                remaining.add(card);
            }
        }
        gameData.playerDecks.get(controllerId).addAll(remaining);
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);

        if (!selectedCardIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " exiles " + selectedCardIds.size()
                            + " card(s) with Vivid and may cast them this turn."));
        }
    }

    private boolean hasColor(Card card, CardColor color) {
        return card.getColors() != null && card.getColors().contains(color);
    }
}
