package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldSelfReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSelfReturnFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedSelfReturnFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedSelfReturnFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedSelfReturnFromGraveyardEffect) effect;

        Card card = entry.getCard();
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        if (ownerId == null) {
            gameLogService.append(gameData, GameLog.cardThen(card, "'s delayed return fizzles - it is no longer in a graveyard."));
            log.info("Game {} - Delayed graveyard return for {} not registered (no longer in graveyard)",
                    gameData.id, card.getName());
            return;
        }

        gameData.queueDelayedAction(new DelayedGraveyardToBattlefieldSelfReturn(
                card.getId(), ownerId, e.counterType(), e.counterAmount(), e.atNextUpkeep(), e.tapped()));
        gameLogService.append(gameData, GameLog.cardThen(card, e.atNextUpkeep()
                ? " will return to the battlefield at the beginning of its owner's next upkeep."
                : " will return to the battlefield at the beginning of the next end step."));
        log.info("Game {} - Delayed graveyard return registered for {} (owner {})",
                gameData.id, card.getName(), ownerId);
    }
}
