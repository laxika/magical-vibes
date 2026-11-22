package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfTargetPermanentsAndDrawPerPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetPlayerGainsControlOfTargetPermanentsAndDrawPerPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerGainsControlOfTargetPermanentsAndDrawPerPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerGainsControlOfTargetPermanentsAndDrawPerPermanentEffect) effect;
        List<UUID> playerTargets = entry.targetsForGroup(e.permanentTargetGroupIndex() - 1);
        UUID newControllerId = playerTargets.stream()
                .filter(gameData.playerIds::contains)
                .findFirst()
                .orElse(null);
        if (newControllerId == null) {
            return;
        }

        int gainedControlCount = 0;
        List<UUID> permanentTargets = entry.targetsForGroup(e.permanentTargetGroupIndex());
        for (UUID permanentId : permanentTargets) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            UUID currentControllerId = permanent == null
                    ? null : gameQueryService.findPermanentController(gameData, permanentId);
            if (permanent == null || currentControllerId == null || currentControllerId.equals(newControllerId)) {
                continue;
            }
            creatureControlService.applyControlEffect(
                    gameData,
                    newControllerId,
                    permanent,
                    new GainControlOfTargetEffect(Duration.PERMANENT),
                    Duration.PERMANENT,
                    null,
                    entry.getCard().getName());
            gainedControlCount++;
        }

        DrawnThisWay snapshot = DrawnThisWay.before(gameData, entry.getControllerId());
        playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), gainedControlCount);
        snapshot.cards(gameData).forEach(card -> entry.recordCardDrawnThisResolution(card.getId()));
    }
}
