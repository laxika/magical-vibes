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
import com.github.laxika.magicalvibes.model.effect.RevealUntilTwoNonlandCardsWithManaValueAndCastOneEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Invasion of Alara's two-card library reveal. */
@Component
@RequiredArgsConstructor
public class RevealUntilTwoNonlandCardsWithManaValueAndCastOneEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilTwoNonlandCardsWithManaValueAndCastOneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var revealEffect = (RevealUntilTwoNonlandCardsWithManaValueAndCastOneEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        List<Card> exiled = new ArrayList<>();
        List<Card> qualifying = new ArrayList<>();
        while (!deck.isEmpty() && qualifying.size() < 2) {
            Card card = deck.removeFirst();
            exiled.add(card);
            if (!card.hasType(CardType.LAND) && card.getManaValue() <= revealEffect.maxManaValue()) {
                qualifying.add(card);
            }
        }

        String sourceName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);
        if (qualifying.isEmpty()) {
            exiled.forEach(card -> gameData.addToExile(controllerId, card));
            gameLogService.append(gameData, GameLog.text(sourceName
                    + " exiles the remaining cards from " + playerName + "'s library without finding two qualifying cards."));
            return;
        }

        exiled.forEach(card -> gameData.addToExile(controllerId, card));
        gameLogService.append(gameData, GameLog.text(playerName + " exiles cards from the top of their library for "
                + sourceName + "."));
        String prompt = qualifying.size() == 1
                ? "You may cast the qualifying card without paying its mana cost."
                : "You may cast one of the two qualifying cards without paying its mana cost.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, new ArrayList<>(qualifying))
                        .reveals(true)
                        .canFailToFind(true)
                        .sourceCards(new ArrayList<>(exiled))
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.CAST_ONE_AND_PUT_OTHER_INTO_HAND)
                        .build(),
                prompt,
                true));
    }
}
