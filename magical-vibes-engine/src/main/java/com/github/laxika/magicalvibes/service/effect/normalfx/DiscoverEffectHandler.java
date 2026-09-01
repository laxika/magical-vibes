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
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the discover keyword action and offers the discovered card through library search. */
@Component
@RequiredArgsConstructor
public class DiscoverEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscoverEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DiscoverEffect discover = (DiscoverEffect) effect;
        UUID controllerId = entry.getControllerId();
        int discoverValue = Math.max(0, amountEvaluationService.evaluate(gameData,
                discover.discoverValue(), AmountContext.forStackEntry(entry, null)));
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            entry.setEventValue(discoverValue);
            triggerCollectionService.checkDiscoverTriggers(gameData, controllerId, discoverValue);
            return;
        }

        List<Card> revealed = new ArrayList<>();
        Card hit = null;
        while (!deck.isEmpty()) {
            Card top = deck.removeFirst();
            revealed.add(top);
            if (!top.hasType(CardType.LAND) && top.getManaValue() <= discoverValue) {
                hit = top;
                break;
            }
        }

        String sourceName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);
        if (hit == null) {
            entry.setEventValue(discoverValue);
            Collections.shuffle(revealed);
            deck.addAll(revealed);
            gameLogService.append(gameData, GameLog.text(sourceName
                    + " (Discover " + discoverValue + "): no qualifying nonland card was found;"
                    + " the revealed cards go to the bottom of " + playerName
                    + "'s library in a random order."));
            triggerCollectionService.checkDiscoverTriggers(gameData, controllerId, discoverValue);
            return;
        }

        entry.setEventValue(Math.max(0, hit.getManaValue()));
        gameLogService.append(gameData, GameLog.text(playerName + " reveals " + hit.getName()
                + " for " + sourceName + " (Discover " + discoverValue + ")."));
        String prompt = "Discover " + discoverValue + " — cast " + hit.getName()
                + " without paying its mana cost or put it into your hand.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, List.of(hit))
                        .reveals(true)
                        .canFailToFind(true)
                        .sourceCards(new ArrayList<>(revealed))
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.DISCOVER)
                        .discoverValue(discoverValue)
                        .build(),
                prompt,
                true));
    }
}
