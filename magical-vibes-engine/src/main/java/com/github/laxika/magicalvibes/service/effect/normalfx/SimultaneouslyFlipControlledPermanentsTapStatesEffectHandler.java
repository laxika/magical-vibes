package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SimultaneouslyFlipControlledPermanentsTapStatesEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link SimultaneouslyFlipControlledPermanentsTapStatesEffect}: snapshot matching
 * permanents the target player controls, then untap the ones that were tapped and tap the ones
 * that were untapped so both halves apply simultaneously.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimultaneouslyFlipControlledPermanentsTapStatesEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final TapUntapSupport tapUntapSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SimultaneouslyFlipControlledPermanentsTapStatesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SimultaneouslyFlipControlledPermanentsTapStatesEffect) effect;
        UUID playerId = entry.getTargetId();
        if (playerId == null || !gameData.playerIds.contains(playerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || battlefield.isEmpty()) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());

        List<Permanent> toUntap = new ArrayList<>();
        List<Permanent> toTap = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (!predicateEvaluationService.matchesPermanentPredicate(permanent, e.filter(), filterContext)) {
                continue;
            }
            if (permanent.isTapped()) {
                toUntap.add(permanent);
            } else {
                toTap.add(permanent);
            }
        }

        int flipped = 0;
        for (Permanent permanent : toUntap) {
            if (tapUntapSupport.untapPermanent(gameData, permanent)) {
                flipped++;
            }
        }
        for (Permanent permanent : toTap) {
            if (tapUntapSupport.tapPermanent(gameData, permanent)) {
                flipped++;
            }
        }

        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" simultaneously flips the tap state of " + flipped + " permanent(s) "
                        + playerName + " controls.")
                .build());
        log.info("Game {} - {} flips tap state of {} permanents controlled by {}",
                gameData.id,
                entry.getCard() != null ? entry.getCard().getName() : "effect",
                flipped,
                playerName);
    }
}
