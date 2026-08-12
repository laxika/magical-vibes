package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTrampleToCreaturesBlockedByTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the combat-specific trample grant used by Ride Down. */
@Component
@RequiredArgsConstructor
public class GrantTrampleToCreaturesBlockedByTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantTrampleToCreaturesBlockedByTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent blocker = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (blocker == null) {
            return;
        }

        List<UUID> blockedCreatureIds = List.copyOf(blocker.getBlockingTargetIds());
        GrantKeywordEffect grant = new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET);
        for (UUID blockedCreatureId : blockedCreatureIds) {
            Permanent blockedCreature = gameQueryService.findPermanentById(gameData, blockedCreatureId);
            if (blockedCreature == null || !gameQueryService.isCreature(gameData, blockedCreature)) {
                continue;
            }
            blockedCreature.getGrantedKeywords().add(Keyword.TRAMPLE);
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(), grant,
                    blockedCreature.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
            gameLogService.append(gameData, GameLog.cardThen(blockedCreature.getCard(), " gains trample until end of turn."));
        }
    }
}
