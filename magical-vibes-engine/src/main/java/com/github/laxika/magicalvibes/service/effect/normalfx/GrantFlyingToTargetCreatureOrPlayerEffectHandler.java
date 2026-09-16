package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantFlyingToTargetCreatureOrPlayerEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
            gameData.playerKeywordsUntilEndOfTurn
                    .computeIfAbsent(targetId, ignored -> ConcurrentHashMap.newKeySet())
                    .add(Keyword.FLYING);
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

        target.getGrantedKeywords().add(Keyword.FLYING);
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                new GrantKeywordEffect(
                        Set.of(Keyword.FLYING),
                        GrantScope.TARGET),
                target.getId(), null, null,
                EffectDuration.UNTIL_END_OF_TURN, 0));
        gameLogService.append(gameData, GameLog.text(target.getCard().getName()
                + " gains flying until end of turn."));
    }
}
