package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSearchesLibraryEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Queues one search per named player ({@link BasicLandSearchQueueSupport}); every count is evaluated before the first
 * search, and the found cards move after the last.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerSearchesLibraryEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final BasicLandSearchQueueSupport basicLandSearchQueueSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerSearchesLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var search = (EachPlayerSearchesLibraryEffect) effect;
        List<UUID> searchers = subjectResolutionService.players(gameData, entry, effect, search.players());
        if (searchers.isEmpty()) {
            return;
        }
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext context = AmountContext.forStackEntry(entry, source);
        boolean tapped = search.destination() == LibrarySearchDestination.BATTLEFIELD_TAPPED;
        List<LibrarySearchFollowUp.BasicLandsPick> picks = new ArrayList<>();
        for (UUID playerId : searchers) {
            picks.add(new LibrarySearchFollowUp.BasicLandsPick(playerId, Math.max(0, amountEvaluationService.evaluate(
                    gameData, search.count(), context.withControllerId(playerId))), tapped));
        }
        LibrarySearchFollowUp followUp = LibrarySearchFollowUp.basicLandSearches(picks, List.of(), true);
        LibrarySearchFollowUp.BasicLandSearchQueue queue = followUp.basicLandSearchQueue()
                .withOptionalSearch(search.optional())
                .withFilter(search.filter())
                .deferBattlefieldEntry();
        if (search.destination() == LibrarySearchDestination.HAND) {
            queue = queue.toHand();
        }
        basicLandSearchQueueSupport.advance(gameData, followUp.withBasicLandSearchQueue(queue));
    }
}
