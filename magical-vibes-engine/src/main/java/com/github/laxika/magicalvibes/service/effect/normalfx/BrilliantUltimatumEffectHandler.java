package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.ChosenOpponentFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BrilliantUltimatumEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link BrilliantUltimatumEffect}: exile the top {@code count} cards of the controller's
 * library, then hand the pile-separation choice to an opponent. Reuses the card-pile separation
 * flow ({@link PendingPileSeparation} with {@code CardPileDisposition.PLAY_FROM_EXILE}); the opponent's pile
 * selection and the controller's pile choice are completed by {@link BrilliantUltimatumSupport}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BrilliantUltimatumEffectHandler implements NormalEffectHandlerBean,
        ChosenOpponentContinuation<ChosenOpponentFollowUp.BrilliantUltimatumPiles> {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final ChosenOpponentSupport chosenOpponentSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BrilliantUltimatumEffect.class;
    }

    @Override
    public Class<ChosenOpponentFollowUp.BrilliantUltimatumPiles> followUpType() {
        return ChosenOpponentFollowUp.BrilliantUltimatumPiles.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BrilliantUltimatumEffect) effect;

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        List<Card> exiledCards = new ArrayList<>();
        for (int i = 0; i < e.count() && deck != null && !deck.isEmpty(); i++) {
            Card card = deck.removeFirst();
            gameData.addToExile(controllerId, card);
            exiledCards.add(card);
        }

        if (exiledCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        String exiledNames = exiledCards.stream().map(Card::getName).collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(playerName + " exiles " + exiledNames + " (" + sourceName + ")."));

        if (ChoosePlayerEffectHandler.onlyOpponent(gameData, controllerId) == null) {
            log.info("Game {} - {} has no opponent to separate the piles", gameData.id, sourceName);
            return;
        }

        gameData.recordPileGroupingOrGuess(entry);
        chosenOpponentSupport.chooseThen(gameData, controllerId, sourceName,
                new ChosenOpponentFollowUp.BrilliantUltimatumPiles(controllerId, exiledCards));
    }

    /** "An opponent separates those cards into two piles": the chosen opponent separates them. */
    @Override
    public void continueWithOpponent(GameData gameData, UUID opponentId,
                                     ChosenOpponentFollowUp.BrilliantUltimatumPiles piles) {
        if (opponentId == null) {
            return;
        }
        Map<UUID, UUID> cardOwners = new HashMap<>();
        piles.exiledCards().forEach(card -> cardOwners.put(card.getId(), piles.controllerId()));
        gameData.queueInteraction(new PendingPileSeparation(piles.controllerId(), opponentId,
                List.of(), piles.exiledCards(), cardOwners, List.of(), List.of(), CardPileDisposition.PLAY_FROM_EXILE));

        interactionHandlerRegistry.begin(
                gameData,
                new PendingInteraction.BrilliantUltimatumPileSeparationChoice(
                        opponentId, piles.exiledCards().stream().map(Card::getId).toList()));
    }
}
