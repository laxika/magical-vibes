package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedLoseLifeAndReturnFromGraveyard;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedLoseLifeAndReturnFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedLoseLifeAndReturnFromGraveyardToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedLoseLifeAndReturnFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedLoseLifeAndReturnFromGraveyardToHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, entry.getCard().getId());
        if (!controllerId.equals(graveyardOwnerId)) {
            return;
        }
        gameData.queueDelayedAction(new DelayedLoseLifeAndReturnFromGraveyard(
                controllerId, entry.getCard(), e.lifeLoss()));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - {} registers delayed lose-life-and-return at next end step for {}",
                gameData.id, playerName, entry.getCard().getName());
    }
}
