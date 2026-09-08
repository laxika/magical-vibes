package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndAllWithSameNameUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Exiles the target permanent and every other permanent sharing its name until the source leaves,
 * then returns each non-token exiled card under its owner's control (Detention Sphere).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentAndAllWithSameNameUntilSourceLeavesEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentAndAllWithSameNameUntilSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID sourcePermanentId = resolveSourcePermanentId(gameData, entry);
        if (sourcePermanentId == null) {
            log.info("Game {} - Source permanent for {} no longer on battlefield, exile without return tracking",
                    gameData.id, entry.getCard().getName());
        }

        ExileTargetPermanentAndAllWithSameNameUntilSourceLeavesEffect sameNameEffect =
                (ExileTargetPermanentAndAllWithSameNameUntilSourceLeavesEffect) effect;
        String targetName = gameQueryService.getEffectiveName(gameData, target);
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                .withSourcePermanentId(entry.getSourcePermanentId());
        List<Permanent> toExile = new ArrayList<>();
        toExile.add(target);
        if (sameNameEffect.sameNameOnlyTargetController()) {
            if (targetControllerId != null) {
                addMatchingPermanents(toExile, gameData.playerBattlefields.get(targetControllerId), target,
                        targetName, sameNameEffect.sameNamePredicate(), filterContext);
            }
        } else {
            gameData.forEachBattlefield((playerId, battlefield) -> addMatchingPermanents(
                    toExile, battlefield, target, targetName, sameNameEffect.sameNamePredicate(), filterContext));
        }

        for (Permanent perm : toExile) {
            Card card = perm.getOriginalCard();
            UUID controllerId = gameQueryService.findPermanentController(gameData, perm.getId());
            UUID ownerId = gameData.stolenCreatures.getOrDefault(perm.getId(), controllerId);
            boolean token = perm.getCard().isToken();

            permanentRemovalService.removePermanentToExile(gameData, perm);
            gameLogService.append(gameData, GameLog.cardTextCard(card, " is exiled by ", entry.getCard(), "."));
            log.info("Game {} - {} exiles {} until it leaves the battlefield",
                    gameData.id, entry.getCard().getName(), card.getName());

            // Tokens cease to exist in exile — nothing to return.
            if (sourcePermanentId != null && !token) {
                gameData.addExileReturnOnPermanentLeave(sourcePermanentId, new PendingExileReturn(card, ownerId));
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private void addMatchingPermanents(List<Permanent> toExile, List<Permanent> battlefield,
                                       Permanent target, String targetName,
                                       PermanentPredicate predicate, FilterContext filterContext) {
        if (battlefield == null) {
            return;
        }
        for (Permanent permanent : battlefield) {
            String permanentName = gameQueryService.getEffectiveName(filterContext.gameData(), permanent);
            if (!permanent.getId().equals(target.getId())
                    && targetName != null
                    && targetName.equals(permanentName)
                    && (predicate == null || predicateEvaluationService.matchesPermanentPredicate(
                            permanent, predicate, filterContext))) {
                toExile.add(permanent);
            }
        }
    }

    private UUID resolveSourcePermanentId(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null
                && gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) != null) {
            return entry.getSourcePermanentId();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return null;
        }
        for (Permanent p : battlefield) {
            if (p.getCard() == entry.getCard()) {
                return p.getId();
            }
        }
        return null;
    }
}
