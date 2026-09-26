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
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves The Ring's level and Ring-bearer designation. */
@Component
@RequiredArgsConstructor
public class RingTemptsYouEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final EffectResolutionService effectResolutionService;
    private final TriggerCollectionService triggerCollectionService;

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

        int previousLevel = gameData.ringLevels.getOrDefault(controllerId, 0);
        gameData.ringLevels.put(controllerId, Math.min(4, previousLevel + 1));
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " is tempted by the Ring."));
        triggerCollectionService.checkRingTemptsYouTriggers(gameData, controllerId);

        List<UUID> creatureIds = gameData.playerBattlefields
                .getOrDefault(controllerId, List.of())
                .stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
        if (creatureIds.isEmpty()) {
            gameData.ringBearerIds.remove(controllerId);
        } else if (creatureIds.size() == 1) {
            gameData.ringBearerIds.put(controllerId, creatureIds.getFirst());
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
            gameData.ringBearerIds.put(context.controllerId(), permanentId);
            gameLogService.append(gameData, GameLog.cardThen(chosen.getCard(),
                    " becomes the Ring-bearer."));
        } else {
            gameData.ringBearerIds.remove(context.controllerId());
        }

        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }
    }
}
