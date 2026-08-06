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
import com.github.laxika.magicalvibes.model.effect.MoveKeywordFromTargetToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link MoveKeywordFromTargetToTargetEffect}: the first target loses the keyword until end
 * of turn, the second gains it until end of turn. Both halves are floated as layer-6 continuous
 * effects (CR 613) wrapping the ordinary {@link RemoveKeywordEffect} / {@link GrantKeywordEffect}
 * payloads, so the layered pass treats them exactly like a standalone loss/grant.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MoveKeywordFromTargetToTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveKeywordFromTargetToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Keyword keyword = ((MoveKeywordFromTargetToTargetEffect) effect).keyword();
        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.isEmpty()) {
            return;
        }

        Permanent loser = entry.isTargetLegal(0)
                ? gameQueryService.findPermanentById(gameData, targets.get(0))
                : null;
        Permanent gainer = targets.size() > 1 && entry.isTargetLegal(1)
                ? gameQueryService.findPermanentById(gameData, targets.get(1))
                : null;

        if (loser != null) {
            RemoveKeywordEffect remove = new RemoveKeywordEffect(keyword, GrantScope.TARGET);
            loser.getRemovedKeywords().add(keyword);
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    entry.getCard().getName(), null, entry.getControllerId(), remove,
                    loser.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
            gameLogService.append(gameData, GameLog.builder().card(loser.getCard())
                    .text(" loses " + label(keyword) + " until end of turn.").build());
        }

        if (gainer != null) {
            GrantKeywordEffect grant = new GrantKeywordEffect(keyword, GrantScope.TARGET);
            gainer.getGrantedKeywords().add(keyword);
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    entry.getCard().getName(), null, entry.getControllerId(), grant,
                    gainer.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
            gameLogService.append(gameData, GameLog.builder().card(gainer.getCard())
                    .text(" gains " + label(keyword) + " until end of turn.").build());
        }

        log.info("Game {} - {} moves {} from {} to {}", gameData.id, entry.getCard().getName(), keyword,
                loser != null ? loser.getCard().getName() : "nothing",
                gainer != null ? gainer.getCard().getName() : "nothing");
    }

    private String label(Keyword keyword) {
        return keyword.name().charAt(0) + keyword.name().substring(1).toLowerCase().replace('_', ' ');
    }
}
