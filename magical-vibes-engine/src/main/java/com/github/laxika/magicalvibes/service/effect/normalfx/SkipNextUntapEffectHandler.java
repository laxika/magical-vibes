package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
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
public class SkipNextUntapEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SkipNextUntapEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SkipNextUntapEffect) effect;
        switch (e.scope()) {
            case TARGET -> resolveTarget(gameData, entry);
            case SELF -> resolveSelf(gameData, entry);
            case TARGET_PLAYERS_PERMANENTS -> resolveTargetPlayersPermanents(gameData, entry, e);
            case ALL_CREATURES -> resolveAllCreatures(gameData, entry, e);
            default -> throw new IllegalStateException("Unsupported skip-next-untap scope: " + e.scope());
        }
    }

    private void resolveSelf(GameData gameData, StackEntry entry) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        source.setSkipUntapCount(source.getSkipUntapCount() + 1);

        String logEntry = source.getCard().getName() + " won't untap during its controller's next untap step.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source.getCard(), " won't untap during its controller's next untap step."));
        log.info("Game {} - {} skip next untap set (self)", gameData.id, source.getCard().getName());
    }

    private void resolveTarget(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        target.setSkipUntapCount(target.getSkipUntapCount() + 1);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(), " won't untap during its controller's next untap step."));
        log.info("Game {} - {} skip next untap set", gameData.id, target.getCard().getName());
    }

    private void resolveTargetPlayersPermanents(GameData gameData, StackEntry entry, SkipNextUntapEffect e) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) return;

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        int count = 0;
        for (Permanent p : battlefield) {
            if (e.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext)) continue;

            p.setSkipUntapCount(p.getSkipUntapCount() + 1);
            count++;
        }

        String logMsg = entry.getCard().getName() + " prevents " + count + " permanent(s) from untapping during their controller's next untap step.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(entry.getCard()).text(" prevents " + count + " permanent(s) from untapping during their controller's next untap step.").build());
        log.info("Game {} - {} skip next untap set on {} permanent(s)", gameData.id, entry.getCard().getName(), count);
    }

    private void resolveAllCreatures(GameData gameData, StackEntry entry, SkipNextUntapEffect e) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        final int[] count = {0};
        gameData.forEachPermanent((playerId, p) -> {
            if (!gameQueryService.isCreature(gameData, p)) return;
            if (e.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext)) return;

            p.setSkipUntapCount(p.getSkipUntapCount() + 1);
            count[0]++;
        });

        String logMsg = entry.getCard().getName() + " prevents " + count[0] + " creature(s) from untapping during their controller's next untap step.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(entry.getCard()).text(" prevents " + count[0] + " creature(s) from untapping during their controller's next untap step.").build());
        log.info("Game {} - {} skip next untap set on {} creature(s)", gameData.id, entry.getCard().getName(), count[0]);
    }
}
