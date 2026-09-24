package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEachPlayerAndExileUpToTwoCreaturesCreateTokenWithTotalPowerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Stitcher Geralf's mill, graveyard choice, and dynamic Zombie token. */
@Component
@RequiredArgsConstructor
public class MillEachPlayerAndExileUpToTwoCreaturesCreateTokenWithTotalPowerEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final CreateTokenEffectHandler createTokenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillEachPlayerAndExileUpToTwoCreaturesCreateTokenWithTotalPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var millEffect = (MillEachPlayerAndExileUpToTwoCreaturesCreateTokenWithTotalPowerEffect) effect;
        var state = gameData.graveyardTargetOperation;
        if (state.milledCreaturesToExileForToken != null
                && state.milledCreaturesToExileForToken.chosenCardIds() != null) {
            List<UUID> chosenCardIds = state.milledCreaturesToExileForToken.chosenCardIds();
            state.milledCreaturesToExileForToken = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
            int totalPower = exileChosenCards(gameData, chosenCardIds);
            createToken(gameData, entry, millEffect, totalPower);
            return;
        }

        List<Card> milled = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            milled.addAll(graveyardService.resolveMillPlayer(gameData, playerId, 3));
        }

        List<Card> eligibleCards = milled.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .filter(card -> gameQueryService.findCardInGraveyardById(gameData, card.getId()) != null)
                .toList();
        if (eligibleCards.isEmpty()) {
            createToken(gameData, entry, millEffect, 0);
            return;
        }

        state.milledCreaturesToExileForToken =
                new GraveyardTargetOperationState.MilledCreaturesToExileForTokenContext(null);
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(
                gameData,
                entry.getControllerId(),
                eligibleCards,
                Math.min(2, eligibleCards.size()),
                "Choose up to two creature cards milled this way to exile.");
    }

    private int exileChosenCards(GameData gameData, List<UUID> chosenCardIds) {
        int totalPower = 0;
        for (UUID cardId : chosenCardIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (card == null || !card.hasType(CardType.CREATURE)) {
                continue;
            }
            if (graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, cardId, card)) {
                totalPower += card.getPower() == null ? 0 : card.getPower();
            }
        }
        return totalPower;
    }

    private void createToken(GameData gameData, StackEntry entry,
                             MillEachPlayerAndExileUpToTwoCreaturesCreateTokenWithTotalPowerEffect effect,
                             int totalPower) {
        createTokenEffectHandler.resolve(gameData, entry,
                effect.tokenTemplate().withPowerToughness(totalPower, totalPower));
    }
}
