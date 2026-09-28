package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillEachPlayerExileUpToTwoCreaturesCreateTokenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Stitcher Geralf's mill, exile, and token-creation ability. */
@Component
@RequiredArgsConstructor
public class MillEachPlayerExileUpToTwoCreaturesCreateTokenEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillEachPlayerExileUpToTwoCreaturesCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var millEffect = (MillEachPlayerExileUpToTwoCreaturesCreateTokenEffect) effect;
        var choiceContext = gameData.graveyardTargetOperation.milledCreatureExile;
        if (choiceContext != null && choiceContext.chosenCardIds() != null) {
            gameData.graveyardTargetOperation.milledCreatureExile = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
            createTokenAfterExile(gameData, entry, millEffect, choiceContext);
            return;
        }

        List<Card> milled = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            milled.addAll(graveyardService.resolveMillPlayer(gameData, playerId, millEffect.count()));
        }

        List<Card> eligibleCards = milled.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .filter(card -> gameQueryService.findCardInGraveyardById(gameData, card.getId()) != null)
                .toList();
        if (eligibleCards.isEmpty()) {
            createToken(gameData, entry, millEffect.tokenTemplate(), 0);
            return;
        }

        gameData.graveyardTargetOperation.milledCreatureExile =
                new GraveyardTargetOperationState.MilledCreatureExileContext(
                        eligibleCards.stream().map(Card::getId).toList(), null);
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(
                gameData,
                entry.getControllerId(),
                eligibleCards,
                2,
                0,
                "Choose up to two creature cards to exile.");
    }

    private void createTokenAfterExile(GameData gameData, StackEntry entry,
                                       MillEachPlayerExileUpToTwoCreaturesCreateTokenEffect effect,
                                       GraveyardTargetOperationState.MilledCreatureExileContext context) {
        int totalPower = 0;
        for (UUID cardId : context.chosenCardIds()) {
            if (!context.eligibleCardIds().contains(cardId)) {
                continue;
            }
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (card == null || !card.hasType(CardType.CREATURE)
                    || !graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, cardId, card)) {
                continue;
            }
            totalPower += card.getPower() == null ? 0 : card.getPower();
        }
        createToken(gameData, entry, effect.tokenTemplate(), totalPower);
    }

    private void createToken(GameData gameData, StackEntry entry, CreateTokenEffect tokenTemplate, int power) {
        entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                gameData,
                entry.getControllerId(),
                tokenTemplate,
                1,
                entry.getCard().getSetCode(),
                power,
                power));
    }
}
