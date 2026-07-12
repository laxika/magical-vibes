package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UntapPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UntapPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (UntapPermanentsEffect) effect;
        switch (e.scope()) {
            case TARGET -> resolveTarget(gameData, entry);
            case ALL_TARGETS -> resolveAllTargets(gameData, entry);
            case SELF -> resolveSelf(gameData, entry);
            case CONTROLLED -> resolveControlled(gameData, entry, e);
            case OTHER_CONTROLLED_CREATURES -> resolveOtherControlledCreatures(gameData, entry, e);
            case TARGET_PLAYERS_PERMANENTS -> resolveTargetPlayersPermanents(gameData, entry, e);
            case ATTACKED_CREATURES -> resolveAttackedCreatures(gameData, entry);
            case ALL_CREATURES -> resolveAllCreatures(gameData, entry, e);
            default -> throw new IllegalStateException("Unsupported untap scope: " + e.scope());
        }
    }

    private void resolveTarget(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        tapUntapSupport.untapPermanent(gameData, target);

        String logEntry = entry.getCard().getName() + " untaps " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} untaps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
    }

    private void resolveAllTargets(GameData gameData, StackEntry entry) {
        List<UUID> targetIds = entry.getTargetIds().isEmpty()
                ? (entry.getTargetId() != null ? List.of(entry.getTargetId()) : List.of())
                : entry.getTargetIds();

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            tapUntapSupport.untapPermanent(gameData, target);

            String logEntry = entry.getCard().getName() + " untaps " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} untaps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
        }
    }

    private void resolveSelf(GameData gameData, StackEntry entry) {
        UUID selfId = entry.getTargetId() != null ? entry.getTargetId() : entry.getSourcePermanentId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        tapUntapSupport.untapPermanent(gameData, self);

        String logEntry = entry.getCard().getName() + " untaps.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} untaps", gameData.id, entry.getCard().getName());
    }

    private void resolveControlled(GameData gameData, StackEntry entry, UntapPermanentsEffect e) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        int count = 0;
        for (Permanent p : battlefield) {
            if (e.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter())) continue;
            if (!p.isTapped()) continue;

            tapUntapSupport.untapPermanent(gameData, p);
            count++;
        }

        String logEntry = entry.getCard().getName() + " untaps " + count + " permanent(s) you control.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} untaps {} controlled permanent(s)", gameData.id, entry.getCard().getName(), count);
    }

    private void resolveOtherControlledCreatures(GameData gameData, StackEntry entry, UntapPermanentsEffect e) {
        UUID controllerId = entry.getControllerId();
        UUID sourceId = entry.getSourcePermanentId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());

        int count = 0;
        for (Permanent p : battlefield) {
            if (p.getId().equals(sourceId)) continue;
            if (!gameQueryService.isCreature(gameData, p)) continue;
            if (e.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext)) continue;
            if (!p.isTapped()) continue;

            tapUntapSupport.untapPermanent(gameData, p);
            count++;
        }

        String logEntry = entry.getCard().getName() + " untaps " + count + " other creature(s) you control.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} untaps {} other creature(s)", gameData.id, entry.getCard().getName(), count);
    }

    private void resolveTargetPlayersPermanents(GameData gameData, StackEntry entry, UntapPermanentsEffect e) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) return;

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());

        int count = 0;
        for (Permanent p : battlefield) {
            if (e.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext)) continue;
            if (!p.isTapped()) continue;

            tapUntapSupport.untapPermanent(gameData, p);
            count++;
        }

        String logEntry = entry.getCard().getName() + " untaps " + count + " permanent(s).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} untaps {} permanent(s) of target player", gameData.id, entry.getCard().getName(), count);
    }

    private void resolveAllCreatures(GameData gameData, StackEntry entry, UntapPermanentsEffect e) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());

        final int[] count = {0};
        gameData.forEachPermanent((playerId, p) -> {
            if (!gameQueryService.isCreature(gameData, p)) return;
            if (e.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext)) return;
            if (!p.isTapped()) return;

            p.untap();
            count[0]++;
        });

        String logEntry = entry.getCard().getName() + " untaps " + count[0] + " creature(s).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} untaps {} creature(s)", gameData.id, entry.getCard().getName(), count[0]);
    }

    private void resolveAttackedCreatures(GameData gameData, StackEntry entry) {
        final int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (!gameQueryService.isCreature(gameData, permanent)) return;
            if (!permanent.isAttackedThisTurn()) return;
            if (!permanent.isTapped()) return;

            tapUntapSupport.untapPermanent(gameData, permanent);
            count[0]++;
        });

        String logEntry = entry.getCard().getName() + " untaps " + count[0] + " creature(s) that attacked this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} untaps {} attacked creature(s)", gameData.id, entry.getCard().getName(), count[0]);
    }
}
