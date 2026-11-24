package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlyingToTargetCreatureOrPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordUntilEndOfTurnEffect;

@Component
@RequiredArgsConstructor
public class GrantFlyingToTargetCreatureOrPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantFlyingToTargetCreatureOrPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.targetsForEffect(effect).stream().findFirst().orElse(null);
        if (targetId == null) {
            return;
        }

        if (gameData.playerIds.contains(targetId)) {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(),
                    new GrantControllerKeywordUntilEndOfTurnEffect(Keyword.FLYING), targetId,
                    Duration.UNTIL_END_OF_TURN));
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(targetId)
                    + " gains flying until end of turn."));
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null
                || !gameQueryService.isCreature(gameData, target)
                || gameQueryService.cantHaveOrGainKeyword(gameData, target, Keyword.FLYING)) {
            return;
        }

        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                new GrantKeywordEffect(
                        Set.of(Keyword.FLYING),
                        Subjects.target()),
                target.getId(), null, null,
                Duration.UNTIL_END_OF_TURN, 0));
        gameLogService.append(gameData, GameLog.text(FaceDownVisibility.publicName(gameData, target)
                + " gains flying until end of turn."));
    }
}
