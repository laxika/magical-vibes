package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOtherNonlandPermanentsWithManaValueAtMostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves {@link ExileOtherNonlandPermanentsWithManaValueAtMostEffect}, the maximum read as it resolves. */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExileOtherNonlandPermanentsWithManaValueAtMostEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOtherNonlandPermanentsWithManaValueAtMostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null
                || gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        int maximumManaValue = amountEvaluationService.evaluate(gameData,
                ((ExileOtherNonlandPermanentsWithManaValueAtMostEffect) effect).maximumManaValue(),
                AmountContext.forStackEntry(entry, source));
        List<Permanent> toExile = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (!sourcePermanentId.equals(permanent.getId())
                        && !gameQueryService.isLand(gameData, permanent)
                        && gameQueryService.getPermanentManaValue(permanent) <= maximumManaValue) {
                    toExile.add(permanent);
                }
            }
        });

        for (Permanent permanent : toExile) {
            if (permanentRemovalService.removePermanentToExile(gameData, permanent)) {
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
                log.info("Game {} - {} is exiled by {}",
                        gameData.id, permanent.getCard().getName(), entry.getCard().getName());
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
