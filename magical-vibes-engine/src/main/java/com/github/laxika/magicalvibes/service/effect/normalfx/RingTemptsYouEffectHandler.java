package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RingTemptsYouEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * "The Ring tempts you" (CR 701.54a, 701.54c): the controller gets the Ring emblem if they lack it and its level
 * rises, then they choose a creature they control as their Ring-bearer (unasked when only one can be chosen; with
 * none, their Ring-bearer stays as it was). The Ring has tempted them once that is done, even if no creature could
 * be chosen (CR 701.54d), which raises the tempted event; a chosen Ring-bearer also raises the bearer-chosen event.
 * Everything is kept in the one Ring store, {@link GameData#ringStates}.
 */
@Component
@RequiredArgsConstructor
public class RingTemptsYouEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final EffectResolutionService effectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RingTemptsYouEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null || !gameData.playerIds.contains(controllerId)) {
            return;
        }

        gameData.temptByRing(controllerId);
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " is tempted by the Ring."));

        List<UUID> creatureIds = gameData.playerBattlefields
                .getOrDefault(controllerId, List.of())
                .stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> !gameQueryService.cantBeChosen(gameData, permanent))
                .map(Permanent::getId)
                .toList();
        if (creatureIds.isEmpty()) {
            ActionEvents.temptedByRing(gameData, controllerId);
        } else if (creatureIds.size() == 1) {
            becomeRingBearer(gameData, controllerId, gameQueryService.findPermanentById(gameData, creatureIds.getFirst()));
        } else {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.RingBearerChoice(controllerId));
            playerInputService.beginPermanentChoice(gameData, controllerId, creatureIds,
                    "Choose a creature you control to be your Ring-bearer.");
        }
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.RingBearerChoice context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen != null
                && gameQueryService.isCreature(gameData, chosen)
                && context.controllerId().equals(gameQueryService.findPermanentController(gameData, permanentId))) {
            becomeRingBearer(gameData, context.controllerId(), chosen);
        } else {
            ActionEvents.temptedByRing(gameData, context.controllerId());
        }

        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }
    }

    private void becomeRingBearer(GameData gameData, UUID controllerId, Permanent chosen) {
        gameData.setRingBearer(controllerId, chosen.getId());
        gameLogService.append(gameData, GameLog.cardThen(chosen.getCard(), " becomes the Ring-bearer."));
        ActionEvents.ringBearerChosen(gameData, controllerId, chosen.getId());
        ActionEvents.temptedByRing(gameData, controllerId);
    }
}
