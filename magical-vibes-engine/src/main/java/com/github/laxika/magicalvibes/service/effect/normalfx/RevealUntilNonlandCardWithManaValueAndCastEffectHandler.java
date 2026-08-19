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
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandCardWithManaValueAndCastEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a library reveal that offers the first qualifying nonland card for a free cast. */
@Component
@RequiredArgsConstructor
public class RevealUntilNonlandCardWithManaValueAndCastEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilNonlandCardWithManaValueAndCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealUntilNonlandCardWithManaValueAndCastEffect revealEffect =
                (RevealUntilNonlandCardWithManaValueAndCastEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        List<Card> revealed = new ArrayList<>();
        Card hit = null;
        while (!deck.isEmpty()) {
            Card top = deck.removeFirst();
            revealed.add(top);
            if (!top.hasType(CardType.LAND) && top.getManaValue() <= revealEffect.maxManaValue()) {
                hit = top;
                break;
            }
        }

        String sourceName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);
        if (hit == null) {
            Collections.shuffle(revealed);
            deck.addAll(revealed);
            gameLogService.append(gameData, GameLog.text(sourceName
                    + ": no qualifying nonland card was found; the revealed cards go to the bottom"
                    + " of " + playerName + "'s library in a random order."));
            return;
        }

        gameLogService.append(gameData, GameLog.text(playerName + " reveals " + hit.getName()
                + " for " + sourceName + "."));
        String prompt = "You may cast " + hit.getName() + " without paying its mana cost.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, List.of(hit))
                        .reveals(true)
                        .canFailToFind(true)
                        .sourceCards(new ArrayList<>(revealed))
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.CAST_WITHOUT_PAYING)
                        .build(),
                prompt,
                true));
    }
}
