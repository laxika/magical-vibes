package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Athreos's return-to-hand trigger and its targeted life-payment choice. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect) effect;
        UUID payingPlayerId = entry.getTargetId();
        if (payingPlayerId == null || !gameData.playerIds.contains(payingPlayerId)) {
            return;
        }

        if (!canPay(gameData, payingPlayerId, e.lifeCost())) {
            returnDyingCard(gameData, e.dyingCardId(), entry.getCard());
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), payingPlayerId, List.of(e),
                "Pay " + e.lifeCost() + " life to prevent the creature from returning to its owner's hand?",
                payingPlayerId));
    }

    public boolean canPay(GameData gameData, UUID playerId, int lifeCost) {
        return gameQueryService.canPlayerLifeChange(gameData, playerId)
                && gameData.getLife(playerId) >= lifeCost;
    }

    /** Returns the dead card to its owner's hand, or fizzles if it has moved since the trigger. */
    public void returnDyingCard(GameData gameData, UUID dyingCardId, Card sourceCard) {
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        Card deadCard = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        if (deadCard == null || ownerId == null) {
            gameLogService.append(gameData,
                    GameLog.cardThen(sourceCard, "'s ability fizzles (the creature is no longer in a graveyard)."));
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCardId);
        gameData.playerHands.get(ownerId).add(deadCard);

        String ownerName = gameData.playerIdToName.get(ownerId);
        gameLogService.append(gameData, GameLog.builder()
                .card(deadCard)
                .text(" returns from graveyard to " + ownerName + "'s hand.")
                .build());
        log.info("Game {} - {} returns {} from graveyard to {}'s hand",
                gameData.id, sourceCard.getName(), deadCard.getName(), ownerName);
    }
}
