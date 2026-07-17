package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsToUntapSelfEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SacrificePermanentsToUntapSelfEffect}: an all-or-nothing "sacrifice N matching
 * permanents; if you do, untap this creature." The controller must control at least {@code count}
 * matching permanents — otherwise the cost cannot be paid and the source is not untapped.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificePermanentsToUntapSelfEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final TapUntapSupport tapUntapSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificePermanentsToUntapSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificePermanentsToUntapSelfEffect) effect;
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        List<Permanent> matching = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter())) {
                    matching.add(p);
                }
            }
        }

        // "If you do" — the whole cost must be payable, otherwise nothing happens (no untap).
        if (matching.size() < e.count()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.text(playerName + " does not sacrifice " + e.description() + "."));
            log.info("Game {} - {} cannot sacrifice {} for {}", gameData.id, playerName,
                    e.description(), entry.getCard().getName());
            return;
        }

        for (int i = 0; i < e.count(); i++) {
            destructionSupport.sacrificeAndLog(gameData, matching.get(i), controllerId);
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null && battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard() == entry.getCard()) {
                    self = p;
                    break;
                }
            }
        }
        if (self != null) {
            tapUntapSupport.untapPermanent(gameData, self);
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.text(entry.getCard().getName() + " untaps."));
            log.info("Game {} - {} untaps", gameData.id, entry.getCard().getName());
        }
    }
}
