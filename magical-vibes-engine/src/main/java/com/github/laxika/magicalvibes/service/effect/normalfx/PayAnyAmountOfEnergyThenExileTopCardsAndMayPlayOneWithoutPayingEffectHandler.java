package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfEnergyThenExileTopCardsAndMayPlayOneWithoutPayingEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the variable-energy impulse ability used by Vault 112: Sadistic Simulation. */
@Component
@RequiredArgsConstructor
@Slf4j
public class PayAnyAmountOfEnergyThenExileTopCardsAndMayPlayOneWithoutPayingEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayAnyAmountOfEnergyThenExileTopCardsAndMayPlayOneWithoutPayingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        int currentEnergy = gameData.playerEnergyCounters.getOrDefault(controllerId, 0);
        if (gameData.chosenXValue == null) {
            if (currentEnergy <= 0) {
                return;
            }
            beginChoice(gameData, entry, currentEnergy);
            return;
        }

        int amount = gameData.chosenXValue;
        gameData.chosenXValue = null;
        if (amount < 0 || amount > currentEnergy) {
            beginChoice(gameData, entry, currentEnergy);
            return;
        }

        gameData.playerEnergyCounters.put(controllerId, currentEnergy - amount);
        String playerName = gameData.playerIdToName.getOrDefault(controllerId, "Player");
        if (amount == 0) {
            gameLogService.append(gameData, GameLog.text(playerName + " pays no energy for "
                    + entry.getCard().getName() + "."));
            return;
        }

        gameLogService.append(gameData, GameLog.text(playerName + " pays " + amount
                + " energy counter(s) for " + entry.getCard().getName() + "."));

        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null) {
            return;
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);

        List<UUID> exiledIds = new ArrayList<>();
        for (int i = 0; i < amount && !deck.isEmpty(); i++) {
            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, controllerId, topCard);
            exiledIds.add(topCard.getId());
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " exiles ").card(topCard)
                    .text(" from the top of their library.").build());
        }

        if (!exiledIds.isEmpty()) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.ExiledCardMayPlayChoice(
                            controllerId, exiledIds, true, false, true, true));
            log.info("Game {} - {} chooses one card among {} exiled cards to play for free",
                    gameData.id, entry.getCard().getName(), exiledIds.size());
        }
    }

    private void beginChoice(GameData gameData, StackEntry entry, int maxValue) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                entry.getControllerId(), maxValue,
                "You may pay any amount of energy for " + entry.getCard().getName()
                        + ". If you pay one or more, shuffle your library, then exile that many cards "
                        + "and play one without paying its mana cost.",
                entry.getCard().getName()));
    }
}
