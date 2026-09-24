package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfTargetPermanentsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetPlayerGainsControlOfTargetPermanentsUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerGainsControlOfTargetPermanentsUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var transfer = (TargetPlayerGainsControlOfTargetPermanentsUntilEndOfTurnEffect) effect;
        List<UUID> playerTargets = entry.targetsForGroup(transfer.permanentTargetGroupIndex() - 1);
        UUID newControllerId = playerTargets.stream()
                .filter(gameData.playerIds::contains)
                .findFirst()
                .orElse(null);
        if (newControllerId == null) {
            return;
        }

        List<UUID> permanentTargets = entry.targetsForGroup(transfer.permanentTargetGroupIndex());
        for (UUID permanentId : permanentTargets) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            UUID currentControllerId = permanent == null
                    ? null : gameQueryService.findPermanentController(gameData, permanentId);
            if (permanent == null || currentControllerId == null || currentControllerId.equals(newControllerId)) {
                continue;
            }

            Card sourceCard = entry.getCard();
            creatureControlService.applyControlEffect(
                    gameData,
                    newControllerId,
                    permanent,
                    new GainControlOfTargetEffect(ControlDuration.END_OF_TURN),
                    EffectDuration.UNTIL_END_OF_TURN,
                    null,
                    sourceCard.getName());
        }
    }
}
