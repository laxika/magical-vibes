package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutMatchingPermanentsOnTopOfOwnersLibrariesEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutMatchingPermanentsOnTopOfOwnersLibrariesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutMatchingPermanentsOnTopOfOwnersLibrariesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutMatchingPermanentsOnTopOfOwnersLibrariesEffect) effect;
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withXValue(entry.getXValue());

        List<UUID> matchingPermanentIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (e.filter() == null || predicateEvaluationService.matchesPermanentPredicate(
                    permanent, e.filter(), context)) {
                matchingPermanentIds.add(permanent.getId());
            }
        });

        beginNextChoice(gameData, entry.getCard().getName(), matchingPermanentIds, List.of(), false);
    }

    public void completeChoice(GameData gameData, List<UUID> chosenPermanentIds,
                               MultiPermanentChoiceContext.PutPermanentsOnTopOfOwnersLibraries context) {
        List<UUID> orderedPermanentIds = new ArrayList<>(context.orderedPermanentIds());
        orderedPermanentIds.addAll(chosenPermanentIds);
        beginNextChoice(gameData, context.sourceCardName(), context.remainingPermanentIds(),
                orderedPermanentIds, true);
    }

    private void beginNextChoice(GameData gameData, String sourceName, List<UUID> pendingPermanentIds,
                                 List<UUID> orderedPermanentIds, boolean resumeAfterCompletion) {
        List<UUID> remainingPermanentIds = pendingPermanentIds.stream()
                .filter(id -> gameQueryService.findPermanentById(gameData, id) != null)
                .toList();
        if (remainingPermanentIds.isEmpty()) {
            finish(gameData, orderedPermanentIds, resumeAfterCompletion);
            return;
        }

        Permanent first = gameQueryService.findPermanentById(gameData, remainingPermanentIds.getFirst());
        UUID ownerId = ownerId(gameData, first);
        List<UUID> ownerPermanentIds = new ArrayList<>();
        List<UUID> laterPermanentIds = new ArrayList<>();
        for (UUID permanentId : remainingPermanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (ownerId.equals(ownerId(gameData, permanent))) {
                ownerPermanentIds.add(permanentId);
            } else {
                laterPermanentIds.add(permanentId);
            }
        }

        if (ownerPermanentIds.size() == 1) {
            List<UUID> updatedOrder = new ArrayList<>(orderedPermanentIds);
            updatedOrder.addAll(ownerPermanentIds);
            beginNextChoice(gameData, sourceName, laterPermanentIds, updatedOrder, resumeAfterCompletion);
            return;
        }

        MultiPermanentChoiceContext.PutPermanentsOnTopOfOwnersLibraries context =
                new MultiPermanentChoiceContext.PutPermanentsOnTopOfOwnersLibraries(
                        laterPermanentIds, orderedPermanentIds, sourceName);
        playerInputService.beginMultiPermanentChoice(gameData, ownerId, ownerPermanentIds,
                ownerPermanentIds.size(), context,
                sourceName + " — Choose the order in which to put these permanents on top of their owners' libraries.");
    }

    private void finish(GameData gameData, List<UUID> orderedPermanentIds, boolean resumeAfterCompletion) {
        for (UUID permanentId : orderedPermanentIds.reversed()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null && permanentRemovalService.removePermanentToLibraryTop(gameData, permanent)) {
                gameLogService.append(gameData,
                        GameLog.cardThen(permanent.getCard(), " is put on top of its owner's library."));
                log.info("Game {} - {} put on top of library", gameData.id, permanent.getCard().getName());
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        if (resumeAfterCompletion) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }

    private UUID ownerId(GameData gameData, Permanent permanent) {
        UUID ownerId = permanent.getCard().getOwnerId();
        if (ownerId == null) {
            ownerId = gameData.defaultControllerOf(permanent.getId());
        }
        if (ownerId == null) {
            ownerId = gameData.currentlyResolvingControllerId;
        }
        return ownerId;
    }
}
