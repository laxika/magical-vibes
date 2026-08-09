package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves a non-targeting choice of opponent who gains control of the source permanent.
 */
@Component
@RequiredArgsConstructor
public class ChooseOpponentGainsControlOfSourceEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseOpponentGainsControlOfSourceEffect.class;
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
            gainControl(gameData, sourcePermanentId, opponents.getFirst(), entry.getCard().getName());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ChooseOpponentGainsControlOfSource(
                sourcePermanentId, entry.getCard().getName()));
        playerInputService.beginAnyTargetChoice(gameData, entry.getControllerId(), List.of(), opponents,
                entry.getCard().getName() + " — choose an opponent.");
    }

    public void completeChoice(GameData gameData, UUID chosenOpponentId,
                               PermanentChoiceContext.ChooseOpponentGainsControlOfSource context) {
        if (!gameData.playerIds.contains(chosenOpponentId)) {
            return;
        }
        gainControl(gameData, context.sourcePermanentId(), chosenOpponentId, context.sourceCardName());
    }

    private void gainControl(GameData gameData, UUID sourcePermanentId, UUID newControllerId,
                             String sourceCardName) {
        var source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return;
        }

        creatureControlService.applyControlEffect(gameData, newControllerId, source,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                ControlDuration.PERMANENT.toEffectDuration(), null, sourceCardName);
    }
}
