package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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
        if (!context.targetIds().contains(permanentId)) {
            return;
        }
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            entry = new StackEntry(context.sourceCard(), context.controllerId());
        }
        placeCounter(gameData, entry, permanentId, context.counterType());
    }

    public void beginCounterPlacement(GameData gameData, StackEntry entry,
                                      List<UUID> targetIds, List<CounterType> counterTypes) {
        List<UUID> liveTargetIds = targetIds.stream()
                .filter(targetId -> gameQueryService.findPermanentById(gameData, targetId) != null)
                .toList();
        if (liveTargetIds.isEmpty() || counterTypes.isEmpty()) {
            return;
        }

        Card sourceCard = entry.getCard();
        UUID controllerId = entry.getControllerId();
        CounterType counterType = counterTypes.getFirst();
        List<CounterType> remainingCounterTypes = counterTypes.subList(1, counterTypes.size());
        if (liveTargetIds.size() == 1) {
            placeCounter(gameData, entry, liveTargetIds.getFirst(), counterType);
            beginCounterPlacement(gameData, entry, liveTargetIds, remainingCounterTypes);
            return;
        }

        PermanentChoiceContext.PutCounterOnEitherTarget context =
                new PermanentChoiceContext.PutCounterOnEitherTarget(
                        sourceCard, controllerId, counterType, liveTargetIds, remainingCounterTypes);
        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPermanentChoice(gameData, controllerId, liveTargetIds, context,
                "Choose a token to receive a " + counterType.name().toLowerCase() + " counter.");
    }

    public void continueCounterPlacement(GameData gameData, UUID permanentId,
                                         PermanentChoiceContext.PutCounterOnEitherTarget context) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            entry = new StackEntry(context.sourceCard(), context.controllerId());
        }
        placeCounter(gameData, entry, permanentId, context.counterType());
        beginCounterPlacement(gameData, entry, context.targetIds(), context.remainingCounterTypes());
    }

    private void placeCounter(GameData gameData, StackEntry entry, UUID permanentId,
                              CounterType counterType) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target != null) {
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, target, counterType, 1);
        }
    }
}
