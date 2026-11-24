package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/**
 * Finds the protected-from player and the permanents once (CR 611.2c), floats {@link ProtectionFromPlayersEffect} on
 * the controller and grants it to each permanent for the duration.
 */
@Component
@RequiredArgsConstructor
public class GrantProtectionFromPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionFromPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantProtectionFromPlayerEffect) effect;
        UUID fromId = subjectResolutionService.player(gameData, entry, effect, grant.from());
        if (fromId == null || !gameData.playerIds.contains(fromId)) {
            return;
        }
        UUID controllerId = entry.getControllerId();
        ProtectionFromPlayersEffect protection = new ProtectionFromPlayersEffect(Set.of(fromId), false);
        String until = Duration.UNTIL_YOUR_NEXT_TURN.equals(grant.duration())
                ? " until their next turn." : " until end of turn.";
        String fromName = gameData.playerIdToName.get(fromId);

        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), controllerId, protection, controllerId, grant.duration()));
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + " gains protection from " + fromName + until));

        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, grant.permanents())) {
            gameData.addEffectOnPermanent(entry, new GrantEffectEffect(protection, Subjects.target()),
                    permanent.getId(), grant.duration());
            gameLogService.append(gameData, GameLog.text(FaceDownVisibility.publicName(gameData, permanent)
                    + " gains protection from " + fromName + until));
        }
    }
}
