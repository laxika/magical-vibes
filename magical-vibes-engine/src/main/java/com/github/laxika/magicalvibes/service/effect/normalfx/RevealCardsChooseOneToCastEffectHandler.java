package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.RevealCardsChooseOneToCastEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Extract Brain's private reveal-and-free-cast choice. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealCardsChooseOneToCastEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealCardsChooseOneToCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealCardsChooseOneToCastEffect castEffect = (RevealCardsChooseOneToCastEffect) effect;
        UUID controllerId = entry.getControllerId();
        int revealCount = Math.max(0, amountEvaluationService.evaluate(
                gameData, castEffect.count(), AmountContext.forStackEntry(entry, null)));
        if (revealCount == 0) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " chooses no cards to look at."));
            log.info("Game {} - {} chooses no cards for Extract Brain", gameData.id,
                    gameData.playerIdToName.get(controllerId));
            return;
        }

        playerInteractionSupport.beginRevealCardsChooseDiscard(
                gameData, entry, revealCount, 1, HandChoiceDestination.KEEP_IN_HAND);
    }
}
