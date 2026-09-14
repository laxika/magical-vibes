package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfNextPlayerNonlandPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfNextPlayerNonlandPermanentsEffect.Direction;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Aminatou's permanent control rotation. */
@Component
@RequiredArgsConstructor
public class GainControlOfNextPlayerNonlandPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfNextPlayerNonlandPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GainControlOfNextPlayerNonlandPermanentsEffect rotation =
                (GainControlOfNextPlayerNonlandPermanentsEffect) effect;
        List<UUID> players = new ArrayList<>(gameData.orderedPlayerIds);
        if (players.size() < 2) {
            return;
        }

        int offset = rotation.direction() == Direction.RIGHT ? 1 : -1;
        List<ControlChange> changes = new ArrayList<>();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        for (int i = 0; i < players.size(); i++) {
            UUID currentControllerId = players.get(i);
            UUID newControllerId = players.get(Math.floorMod(i + offset, players.size()));
            List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(currentControllerId, List.of());
            for (Permanent permanent : List.copyOf(battlefield)) {
                if (permanent.getId().equals(sourcePermanentId)
                        || gameQueryService.isLand(gameData, permanent)) {
                    continue;
                }
                changes.add(new ControlChange(permanent, newControllerId));
            }
        }

        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(
                rotation.controlDuration());
        for (ControlChange change : changes) {
            creatureControlService.applyControlEffect(
                    gameData, change.newControllerId(), change.permanent(), controlEffect,
                    rotation.controlDuration().toEffectDuration(), null, entry.getCard().getName());
        }
    }

    private record ControlChange(Permanent permanent, UUID newControllerId) {
    }
}
