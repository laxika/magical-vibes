package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedEffectOnDeath;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureCardFromTargetOwnerGraveyardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResolveEffectOnTargetDeathThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ResolveEffectOnTargetDeathThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ResolveEffectOnTargetDeathThisTurnEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            log.info("Game {} - Target permanent no longer on battlefield, delayed death trigger not registered", gameData.id);
            return;
        }

        CardEffect delayedEffect = e.effect();
        if (delayedEffect instanceof ReturnCreatureCardFromTargetOwnerGraveyardEffect returnEffect) {
            UUID ownerId = target.getCard().getOwnerId();
            if (ownerId == null) {
                ownerId = gameQueryService.findPermanentController(gameData, target.getId());
            }
            delayedEffect = returnEffect.bindOwner(ownerId);
        }

        gameData.permanentTriggeringEffectOnDeathThisTurn
                .computeIfAbsent(target.getCard().getId(), k -> Collections.synchronizedList(new ArrayList<>()))
                .add(new DelayedEffectOnDeath(delayedEffect, entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId()));

        log.info("Game {} - Delayed trigger registered: if {} dies this turn, {} triggers",
                gameData.id, target.getCard().getName(), entry.getCard().getName());
    }
}
