package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.NextPlayerGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a directional permanent control handoff of the source. */
@Component
@RequiredArgsConstructor
public class NextPlayerGainsControlOfSourceEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return NextPlayerGainsControlOfSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        NextPlayerGainsControlOfSourceEffect handoff =
                (NextPlayerGainsControlOfSourceEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        List<UUID> players = new ArrayList<>(gameData.orderedPlayerIds);
        int controllerIndex = players.indexOf(entry.getControllerId());
        if (controllerIndex < 0 || players.size() < 2) {
            return;
        }

        UUID newControllerId = players.get(Math.floorMod(
                controllerIndex + handoff.direction().turnOrderOffset(), players.size()));
        creatureControlService.applyControlEffect(
                gameData,
                newControllerId,
                source,
                new GainControlOfTargetEffect(handoff.controlDuration()),
                handoff.controlDuration().toEffectDuration(),
                null,
                entry.getCard().getName());
    }
}
