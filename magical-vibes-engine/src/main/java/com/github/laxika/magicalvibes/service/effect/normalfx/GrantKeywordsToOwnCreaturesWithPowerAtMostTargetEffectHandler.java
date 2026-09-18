package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToOwnCreaturesWithPowerAtMostTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Resolves a power-relative keyword grant to the controller's creatures. */
@Component
@RequiredArgsConstructor
public class GrantKeywordsToOwnCreaturesWithPowerAtMostTargetEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantKeywordsToOwnCreaturesWithPowerAtMostTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantKeywordsToOwnCreaturesWithPowerAtMostTargetEffect) effect;
        UUID targetId = entry.targetsForEffect(effect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        Permanent target = targetId == null
                ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isLand(gameData, target)) {
            return;
        }

        int targetPower = gameQueryService.getEffectivePower(gameData, target);
        int count = 0;
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of())) {
            if (!gameQueryService.isCreature(gameData, permanent)
                    || gameQueryService.getEffectivePower(gameData, permanent) > targetPower) {
                continue;
            }

            Set<Keyword> grantableKeywords = grant.keywords().stream()
                    .filter(keyword -> !gameQueryService.cantHaveOrGainKeyword(gameData, permanent, keyword))
                    .collect(Collectors.toSet());
            if (grantableKeywords.isEmpty()) {
                continue;
            }

            permanent.getGrantedKeywords().addAll(grantableKeywords);
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                    new GrantKeywordEffect(grantableKeywords, GrantScope.TARGET),
                    permanent.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
            count++;
        }

        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(" gives " + formatKeywords(grant.keywords()) + " to " + count
                        + " creature(s) until end of turn.").build());
    }

    private String formatKeywords(Set<Keyword> keywords) {
        return keywords.stream()
                .map(keyword -> keyword.name().charAt(0)
                        + keyword.name().substring(1).toLowerCase().replace('_', ' '))
                .collect(Collectors.joining(", "));
    }
}
