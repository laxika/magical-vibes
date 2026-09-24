package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfNextPlayerNonlandPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the directional permanent-control exchange used by Aminatou's ultimate. */
@Component
@RequiredArgsConstructor
public class GainControlOfNextPlayerNonlandPermanentsEffectHandler implements NormalEffectHandlerBean {

    private static final GainControlOfTargetEffect CONTROL_EFFECT =
            new GainControlOfTargetEffect(ControlDuration.PERMANENT);

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfNextPlayerNonlandPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GainControlOfNextPlayerNonlandPermanentsEffect directionalEffect =
                (GainControlOfNextPlayerNonlandPermanentsEffect) effect;

        List<UUID> players = new ArrayList<>();
        synchronized (gameData.orderedPlayerIds) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (gameData.playerIds.contains(playerId)) {
                    players.add(playerId);
                }
            }
        }
        if (players.size() < 2) {
            return;
        }

        UUID sourcePermanentId = entry.getSourcePermanentId();
        List<ControlledPermanent> permanents = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (!gameQueryService.isLand(gameData, permanent)
                    && (sourcePermanentId == null || !sourcePermanentId.equals(permanent.getId()))) {
                permanents.add(new ControlledPermanent(controllerId, permanent));
            }
        });

        int offset = directionalEffect.direction().turnOrderOffset();
        for (ControlledPermanent controlledPermanent : permanents) {
            int controllerIndex = players.indexOf(controlledPermanent.controllerId());
            if (controllerIndex < 0) {
                continue;
            }
            UUID newControllerId = players.get(Math.floorMod(controllerIndex + offset, players.size()));
            if (!newControllerId.equals(controlledPermanent.controllerId())) {
                creatureControlService.applyControlEffect(gameData, newControllerId,
                        controlledPermanent.permanent(), CONTROL_EFFECT,
                        ControlDuration.PERMANENT.toEffectDuration(), null,
                        entry.getCard().getName());
            }
        }
    }

    private record ControlledPermanent(UUID controllerId, Permanent permanent) {
    }
}
