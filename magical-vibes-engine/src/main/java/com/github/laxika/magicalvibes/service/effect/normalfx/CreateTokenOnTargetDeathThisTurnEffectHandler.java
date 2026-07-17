package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedTokenOnDeath;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenOnTargetDeathThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenOnTargetDeathThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenOnTargetDeathThisTurnEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            log.info("Game {} - Target creature no longer on battlefield, delayed token trigger not registered", gameData.id);
            return;
        }

        gameData.creatureCreatingTokenOnDeathThisTurn
                .computeIfAbsent(target.getCard().getId(), k -> Collections.synchronizedList(new ArrayList<>()))
                .add(new DelayedTokenOnDeath(e.tokenEffect(), entry.getControllerId(), entry.getCard()));

        log.info("Game {} - Delayed trigger registered: if {} dies this turn, its controller creates a token",
                gameData.id, target.getCard().getName());
    }
}
