package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEitherTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutCounterOnEitherTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnEitherTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutCounterOnEitherTargetPermanentEffect counterEffect =
                (PutCounterOnEitherTargetPermanentEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect).stream()
                .filter(targetId -> gameQueryService.findPermanentById(gameData, targetId) != null)
                .toList();
        if (targetIds.isEmpty()) {
            return;
        }

        if (targetIds.size() == 1) {
            placeCounter(gameData, entry, targetIds.getFirst(), counterEffect.counterType());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.PutCounterOnEitherTarget(
                entry.getCard(), entry.getControllerId(), counterEffect.counterType(), targetIds));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), targetIds,
                "Choose a creature to receive a " + counterEffect.counterType().name().toLowerCase()
                        + " counter.");
    }

    public void placeCounter(GameData gameData, UUID permanentId,
                             PermanentChoiceContext.PutCounterOnEitherTarget context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null || !context.targetIds().contains(permanentId)) {
            return;
        }

        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            entry = new StackEntry(context.sourceCard(), context.controllerId());
        }
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, target, context.counterType(), 1);
    }

    private void placeCounter(GameData gameData, StackEntry entry, UUID permanentId,
                               com.github.laxika.magicalvibes.model.CounterType counterType) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target != null) {
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, target, counterType, 1);
        }
    }
}
