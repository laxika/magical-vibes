package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingReturnExiledWithSourceCard;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfDamagedPlayerLibraryAndPutCreatureOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardOntoBattlefieldUnderControllerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Lord of the Void's combat-damage library exile and creature theft. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsOfDamagedPlayerLibraryAndPutCreatureOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final PutExiledCardOntoBattlefieldUnderControllerEffectHandler putExiledCardHandler;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsOfDamagedPlayerLibraryAndPutCreatureOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (ExileTopCardsOfDamagedPlayerLibraryAndPutCreatureOntoBattlefieldEffect) effect;
        UUID damagedPlayerId = entry.getTargetId();
        if (damagedPlayerId == null) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(damagedPlayerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        int count = Math.min(typedEffect.count(), library.size());
        List<Card> exiledCards = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Card card = library.removeFirst();
            exileService.exileCard(gameData, damagedPlayerId, card);
            exiledCards.add(card);
        }

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(damagedPlayerId) + " exiles " + count
                        + " card" + (count == 1 ? "" : "s") + " from the top of their library."));

        List<Card> creatureCards = exiledCards.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .toList();
        if (creatureCards.isEmpty()) {
            return;
        }

        if (creatureCards.size() == 1) {
            putExiledCardHandler.resolve(gameData, entry,
                    new PutExiledCardOntoBattlefieldUnderControllerEffect(creatureCards.getFirst().getId()));
            return;
        }

        gameData.queueInteraction(new PendingReturnExiledWithSourceCard(true, entry.getControllerId()));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                entry.getControllerId(), exiledCards, creatureCards.stream().map(Card::getId).toList(),
                1, "Choose a creature card to put onto the battlefield.", false));
    }
}
