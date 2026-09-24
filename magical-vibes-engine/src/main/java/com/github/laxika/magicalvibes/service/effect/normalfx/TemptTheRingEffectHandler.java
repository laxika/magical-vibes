package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.RingState;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TemptTheRingEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TemptTheRingEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Autowired
    @Lazy
    private TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TemptTheRingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        RingState current = gameData.ringStates.get(controllerId);
        UUID currentBearerId = current == null ? null : current.bearerId();
        if (currentBearerId != null && gameQueryService.findPermanentById(gameData, currentBearerId) == null) {
            currentBearerId = null;
        }
        RingState next = current == null
                ? new RingState(1, currentBearerId)
                : current.tempt(currentBearerId);
        gameData.ringStates.put(controllerId, next);

        List<UUID> eligibleIds = gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
        if (eligibleIds.isEmpty()) {
            return;
        }
        if (eligibleIds.size() == 1) {
            UUID bearerId = eligibleIds.getFirst();
            setBearer(gameData, controllerId, bearerId);
            triggerCollectionService.checkRingTemptTriggers(gameData, controllerId, bearerId);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.RingBearerChoice(controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, eligibleIds,
                "Choose a creature you control to be your Ring-bearer.");
    }

    public void completeChoice(GameData gameData, UUID chosenPermanentId,
                               PermanentChoiceContext.RingBearerChoice context) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null || !entry.getControllerId().equals(context.controllerId())) {
            throw new IllegalStateException("No effect is waiting for a Ring-bearer choice");
        }
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null || !gameQueryService.isCreature(gameData, chosen)
                || !context.controllerId().equals(
                gameQueryService.findPermanentController(gameData, chosenPermanentId))) {
            throw new IllegalStateException("Choose a creature you control to be your Ring-bearer");
        }
        setBearer(gameData, context.controllerId(), chosenPermanentId);
        triggerCollectionService.checkRingTemptTriggers(gameData, context.controllerId(), chosenPermanentId);
    }

    private void setBearer(GameData gameData, UUID controllerId, UUID bearerId) {
        RingState current = gameData.ringStates.get(controllerId);
        int level = current == null ? 1 : current.level();
        gameData.ringStates.put(controllerId, new RingState(level, bearerId));
    }
}
