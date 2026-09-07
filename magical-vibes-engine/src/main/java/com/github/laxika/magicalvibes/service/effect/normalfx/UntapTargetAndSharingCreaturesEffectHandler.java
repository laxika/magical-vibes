package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetAndSharingCreaturesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UntapTargetAndSharingCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UntapTargetAndSharingCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var untapEffect = (UntapTargetAndSharingCreaturesEffect) effect;
        UUID targetId = entry.targetsForEffect(untapEffect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        Set<CardColor> targetColors = gameQueryService.getEffectiveColors(gameData, target);
        int[] affectedCount = {0};
        gameData.forEachPermanent((ignored, permanent) -> {
            if (!gameQueryService.isCreature(gameData, permanent)
                    || (!permanent.getId().equals(targetId)
                    && (targetColors.isEmpty()
                    || gameQueryService.getEffectiveColors(gameData, permanent).stream()
                    .noneMatch(targetColors::contains)))) {
                return;
            }
            if (tapUntapSupport.untapPermanent(gameData, permanent)) {
                affectedCount[0]++;
            }
        });

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(String.format(" untaps %d creature(s).", affectedCount[0]))
                .build());
        log.info("Game {} - {} untaps {} color-sharing creatures", gameData.id,
                entry.getCard().getName(), affectedCount[0]);
    }
}
