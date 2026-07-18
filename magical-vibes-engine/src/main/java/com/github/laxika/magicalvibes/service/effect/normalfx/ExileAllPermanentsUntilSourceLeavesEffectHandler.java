package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
 * Exiles every permanent matching the effect's filter until the source leaves the battlefield,
 * then returns each exiled card (tapped iff requested). Combines the mass-exile scan of
 * {@link ExileAllPermanentsEffectHandler} with the O-ring return linkage. Used by Realm Razer.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileAllPermanentsUntilSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAllPermanentsUntilSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileAllPermanentsUntilSourceLeavesEffect) effect;

        UUID sourcePermanentId = findSourcePermanentId(gameData, entry);
        if (sourcePermanentId == null) {
            log.info("Game {} - Source permanent for {} no longer on battlefield, exile without return tracking",
                    gameData.id, entry.getCard().getName());
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        List<Permanent> toExile = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(perm, e.filter(), filterContext)) {
                    toExile.add(perm);
                }
            }
        });

        for (Permanent perm : toExile) {
            Card card = perm.getOriginalCard();
            UUID controllerId = gameQueryService.findPermanentController(gameData, perm.getId());
            UUID ownerId = gameData.stolenCreatures.getOrDefault(perm.getId(), controllerId);

            permanentRemovalService.removePermanentToExile(gameData, perm);

            String logEntry = card.getName() + " is exiled by " + entry.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(card, " is exiled by ", entry.getCard(), "."));
            log.info("Game {} - {} exiles {} until it leaves the battlefield",
                    gameData.id, entry.getCard().getName(), card.getName());

            if (sourcePermanentId != null) {
                gameData.addExileReturnOnPermanentLeave(sourcePermanentId,
                        new PendingExileReturn(card, ownerId, e.returnTapped()));
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private UUID findSourcePermanentId(GameData gameData, StackEntry entry) {
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
