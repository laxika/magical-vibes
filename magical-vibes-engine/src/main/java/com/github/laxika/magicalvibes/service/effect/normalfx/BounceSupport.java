package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shared bounce helpers used by every "normal" Bounce effect handler.
 *
 * <p>Extracted verbatim from {@code BounceResolutionService}; behavior is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BounceSupport {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final StateTriggerService stateTriggerService;

    public void applyReturnSelfToHand(GameData gameData, StackEntry entry) {
        Permanent toReturn = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());

        if (toReturn == null) {
            
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " is no longer on the battlefield."));
            return;
        }

        permanentRemovalService.removePermanentToHand(gameData, toReturn);
        permanentRemovalService.removeOrphanedAuras(gameData);

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " is returned to its owner's hand."));
        log.info("Game {} - {} returned to hand", gameData.id, entry.getCard().getName());
    }

    public void applyReturnAllPermanentsOfColorToHand(GameData gameData, StackEntry entry, CardColor color) {
        applyReturnAllPermanentsOfColorToHand(gameData, entry, color, null);
    }

    public void applyReturnAllPermanentsOfColorToHand(GameData gameData, StackEntry entry, CardColor color,
            PermanentPredicate filter) {
        List<Permanent> toReturn = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) ->
                toReturn.addAll(battlefield.stream()
                        .filter(permanent -> gameQueryService.getEffectiveColors(gameData, permanent).contains(color)
                                && (filter == null || predicateEvaluationService.matchesPermanentPredicate(
                                gameData, permanent, filter)))
                        .toList()));

        for (Permanent permanent : toReturn) {
            permanentRemovalService.removePermanentToHand(gameData, permanent);

            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is returned to its owner's hand."));
            log.info("Game {} - {} returned to owner's hand by {}",
                    gameData.id, permanent.getCard().getName(), entry.getCard().getName());
        }

        if (!toReturn.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }

    /**
     * Removes a spell from the stack and returns its card to its owner's hand. Not countering —
     * uncounterable spells are still bounced; copies cease to exist. Abilities on the stack are
     * ignored (only true spells are returned).
     */
    public void returnSpellToOwnerHand(GameData gameData, StackEntry source, UUID targetCardId) {
        StackEntry target = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                target = se;
                break;
            }
        }
        if (target == null) {
            log.info("Game {} - Spell bounce target no longer on stack", gameData.id);
            return;
        }

        StackEntryType type = target.getEntryType();
        if (type == StackEntryType.ACTIVATED_ABILITY || type == StackEntryType.TRIGGERED_ABILITY) {
            log.info("Game {} - Spell bounce ignores ability on stack", gameData.id);
            return;
        }

        gameData.stack.remove(target);
        stateTriggerService.cleanupResolvedStateTrigger(gameData, target);

        if (!target.isCopy()) {
            Card spell = target.getPhysicalCard();
            UUID ownerId = spell.getOwnerId() != null ? spell.getOwnerId() : target.getControllerId();
            gameData.playerHands.computeIfAbsent(ownerId, id -> new java.util.ArrayList<>()).add(spell);
        }

        gameLogService.append(gameData,
                GameLog.cardThen(target.getCard(), " is returned to its owner's hand."));
        log.info("Game {} - {} returned {} to owner's hand",
                gameData.id, source.getCard().getName(), target.getCard().getName());
    }
}
