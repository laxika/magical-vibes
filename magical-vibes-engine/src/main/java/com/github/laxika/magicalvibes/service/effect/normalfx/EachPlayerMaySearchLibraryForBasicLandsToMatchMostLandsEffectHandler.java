package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMaySearchLibraryForBasicLandsToMatchMostLandsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the Scholarship Sponsor land-count catch-up search. */
@Component
@RequiredArgsConstructor
public class EachPlayerMaySearchLibraryForBasicLandsToMatchMostLandsEffectHandler
        implements NormalEffectHandlerBean {

    private final BasicLandSearchQueueSupport basicLandSearchQueueSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMaySearchLibraryForBasicLandsToMatchMostLandsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> apnapOrder = basicLandSearchQueueSupport.apnapOrder(gameData);
        PermanentIsLandPredicate isLand = new PermanentIsLandPredicate();
        Map<UUID, Integer> landCounts = new HashMap<>();
        int mostLands = 0;

        for (UUID playerId : apnapOrder) {
            int count = (int) gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                    .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                            gameData, permanent, isLand))
                    .count();
            landCounts.put(playerId, count);
            mostLands = Math.max(mostLands, count);
        }

        int landLeaderCount = mostLands;
        List<LibrarySearchFollowUp.BasicLandsPick> picks = apnapOrder.stream()
                .map(playerId -> new LibrarySearchFollowUp.BasicLandsPick(
                        playerId, landLeaderCount - landCounts.get(playerId), true))
                .filter(pick -> pick.count() > 0)
                .toList();

        if (!picks.isEmpty()) {
            basicLandSearchQueueSupport.advance(
                    gameData, LibrarySearchFollowUp.basicLandSearches(picks, List.of()));
        }
    }
}
