package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseAnotherPlayerGainsControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves a non-targeting choice of another player who gains control of the target permanent.
 */
@Component
@RequiredArgsConstructor
public class ChooseAnotherPlayerGainsControlOfTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseAnotherPlayerGainsControlOfTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPermanentId = entry.targetsForEffect(effect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        if (targetPermanentId == null || gameQueryService.findPermanentById(gameData, targetPermanentId) == null) {
            return;
        }

        List<UUID> otherPlayerIds = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .toList();
        if (otherPlayerIds.isEmpty()) {
            return;
        }

        if (otherPlayerIds.size() == 1) {
            gainControl(gameData, targetPermanentId, otherPlayerIds.getFirst(), entry.getCard().getName());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ChooseAnotherPlayerGainsControlOfTargetPermanent(
                        entry.getControllerId(), targetPermanentId, entry.getCard().getName()));
        playerInputService.beginPlayerChoice(gameData, entry.getControllerId(), otherPlayerIds,
                entry.getCard().getName() + " - choose another player.");
    }

    public void completeChoice(GameData gameData, UUID chosenPlayerId,
                               PermanentChoiceContext.ChooseAnotherPlayerGainsControlOfTargetPermanent context) {
        if (!gameData.playerIds.contains(chosenPlayerId) || chosenPlayerId.equals(context.controllerId())) {
            return;
        }
        gainControl(gameData, context.targetPermanentId(), chosenPlayerId, context.sourceCardName());
    }

    private void gainControl(GameData gameData, UUID targetPermanentId, UUID newControllerId,
                             String sourceCardName) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target == null) {
            return;
        }

        creatureControlService.applyControlEffect(gameData, newControllerId, target,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                ControlDuration.PERMANENT.toEffectDuration(), null, sourceCardName);
    }
}
