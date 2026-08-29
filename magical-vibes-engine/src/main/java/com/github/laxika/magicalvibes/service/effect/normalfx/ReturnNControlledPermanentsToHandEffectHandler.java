package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnNControlledPermanentsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a mandatory choice of matching permanents controlled by the resolving player. */
@Component
@RequiredArgsConstructor
public class ReturnNControlledPermanentsToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnNControlledPermanentsToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnNControlledPermanentsToHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> validIds = matchingControlledPermanentIds(gameData, entry, e);
        if (validIds.isEmpty()) {
            return;
        }

        int count = Math.max(0, e.count());
        if (count == 0) {
            return;
        }
        if (validIds.size() <= count) {
            returnPermanents(gameData, validIds, controllerId, e.noun(), entry.getCard(),
                    entry.getSourcePermanentId());
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, controllerId, validIds, count,
                new MultiPermanentChoiceContext.ReturnNControlledPermanentsToHand(e),
                "Choose exactly " + count + " " + plural(e.noun(), count) + " you control to return.");
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.ReturnNControlledPermanentsToHand context,
                               StackEntry entry) {
        ReturnNControlledPermanentsToHandEffect effect = context.effect();
        List<UUID> validIds = matchingControlledPermanentIds(gameData, entry, effect);
        List<UUID> chosenIds = permanentIds.stream().filter(validIds::contains).toList();
        returnPermanents(gameData, chosenIds, entry.getControllerId(), effect.noun(), entry.getCard(),
                entry.getSourcePermanentId());
    }

    private List<UUID> matchingControlledPermanentIds(GameData gameData, StackEntry entry,
                                                       ReturnNControlledPermanentsToHandEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> validIds = new ArrayList<>();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if (effect.filter() == null || predicateEvaluationService.matchesPermanentPredicate(
                    permanent, effect.filter(), filterContext)) {
                validIds.add(permanent.getId());
            }
        }
        return validIds;
    }

    private void returnPermanents(GameData gameData, List<UUID> permanentIds, UUID controllerId,
                                  String noun, Card sourceCard, UUID sourcePermanentId) {
        List<Card> bouncedCards = new ArrayList<>();
        List<UUID> orderedPermanentIds = new ArrayList<>(permanentIds);
        if (sourcePermanentId != null && orderedPermanentIds.remove(sourcePermanentId)) {
            orderedPermanentIds.add(sourcePermanentId);
        }
        for (UUID permanentId : orderedPermanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null && controllerId.equals(gameQueryService.findPermanentController(gameData, permanentId))
                    && permanentRemovalService.removePermanentToHand(gameData, permanent)) {
                bouncedCards.add(permanent.getCard());
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        if (!bouncedCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                    " returns " + bouncedCards.size() + " " + plural(noun, bouncedCards.size())
                            + " to their owners' hands."));
        }
    }

    private static String plural(String noun, int count) {
        String resolvedNoun = noun == null ? "permanent" : noun;
        return count == 1 ? resolvedNoun : resolvedNoun + "s";
    }
}
