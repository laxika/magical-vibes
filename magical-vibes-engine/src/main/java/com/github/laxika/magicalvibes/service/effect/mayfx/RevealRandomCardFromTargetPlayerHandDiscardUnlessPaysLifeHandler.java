package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandDiscardUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RevealRandomCardFromTargetPlayerHandDiscardUnlessPaysLifeEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Handles Wand of Ith's pay-or-discard decision for the revealed card. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealRandomCardFromTargetPlayerHandDiscardUnlessPaysLifeHandler
        implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final RevealRandomCardFromTargetPlayerHandDiscardUnlessPaysLifeEffectHandler effectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealRandomCardFromTargetPlayerHandDiscardUnlessPaysLifeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = ability.effects().stream()
                .filter(RevealRandomCardFromTargetPlayerHandDiscardUnlessPaysLifeEffect.class::isInstance)
                .map(RevealRandomCardFromTargetPlayerHandDiscardUnlessPaysLifeEffect.class::cast)
                .findFirst()
                .orElseThrow();

        UUID playerId = ability.controllerId();
        Card revealed = findInHand(gameData, playerId, ability.targetCardId());
        if (revealed == null) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        int lifeCost = effectHandler.lifeCost(revealed);
        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, playerId)
                && gameData.getLife(playerId) >= lifeCost;
        if (accepted && canPay) {
            int lifeLoss = lifeCost * gameQueryService.opponentLifeLossMultiplier(gameData, playerId);
            gameData.playerLifeTotals.put(playerId, gameData.getLife(playerId) - lifeLoss);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " pays " + lifeLoss + " life. (", ability.sourceCard(), ")"));
            log.info("Game {} - {} pays {} life to keep {} ({})", gameData.id,
                    player.getUsername(), lifeLoss, revealed.getName(), ability.sourceCard().getName());
        } else {
            effectHandler.discardCard(gameData, playerId, revealed.getId(), ability.sourceCard(),
                    ability.sourceControllerId());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private Card findInHand(GameData gameData, UUID playerId, UUID cardId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || cardId == null) {
            return null;
        }
        return hand.stream().filter(card -> card.getId().equals(cardId)).findFirst().orElse(null);
    }
}
