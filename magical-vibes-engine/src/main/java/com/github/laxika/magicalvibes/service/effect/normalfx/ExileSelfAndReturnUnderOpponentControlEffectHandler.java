package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnUnderOpponentControlEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a source permanent's immediate return under a chosen opponent's control. */
@Component
@RequiredArgsConstructor
public class ExileSelfAndReturnUnderOpponentControlEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final FlickerEffectHandler flickerEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSelfAndReturnUnderOpponentControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null || gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
            return;
        }

        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .toList();
        if (opponents.isEmpty()) {
            return;
        }

        if (opponents.size() == 1) {
            flickerEffectHandler.flickerSelfUnderControl(gameData, entry, opponents.getFirst());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ChooseOpponentForSelfFlicker(
                sourcePermanentId, entry.getControllerId(), entry.getCard().getName()));
        playerInputService.beginPlayerChoice(gameData, entry.getControllerId(), opponents,
                entry.getCard().getName() + " — choose an opponent.");
    }

    public void completeChoice(GameData gameData, UUID chosenOpponentId,
                               PermanentChoiceContext.ChooseOpponentForSelfFlicker context) {
        if (!gameData.playerIds.contains(chosenOpponentId)
                || chosenOpponentId.equals(context.controllerId())) {
            return;
        }
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        Permanent source = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
        if (entry == null || source == null) {
            return;
        }
        flickerEffectHandler.flickerSelfUnderControl(gameData, entry, chosenOpponentId);
    }
}
