package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMatchingPermanentsUnlessControllerPaysEffect;
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
 * Resolves {@link ReturnMatchingPermanentsUnlessControllerPaysEffect}: every permanent matching the
 * filter gets an independent pay-or-be-bounced decision made by its own controller, offered one at a
 * time (remaining permanent ids live in {@link GameData#bounceUnlessPaysRemaining}) in APNAP order.
 *
 * <p>The prompt is always shown even when the controller has no floating mana — paying is a choice
 * and mana abilities may still be activated while the prompt is up; accepting without the mana falls
 * through to the bounce in {@code MayPenaltyChoiceHandlerService}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnMatchingPermanentsUnlessControllerPaysEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnMatchingPermanentsUnlessControllerPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnMatchingPermanentsUnlessControllerPaysEffect) effect;
        List<UUID> matching = matchingPermanents(gameData, e);

        gameData.bounceUnlessPaysRemaining.clear();
        if (matching.isEmpty()) {
            return;
        }
        gameData.bounceUnlessPaysRemaining.addAll(matching.subList(1, matching.size()));
        offerNext(gameData, e, entry.getCard(), matching.getFirst());
    }

    /**
     * After a decision on one permanent: bounce it when its controller declined or couldn't pay, then
     * offer the next queued permanent. Called from {@code MayPenaltyChoiceHandlerService}.
     */
    public void afterPermanentDecision(GameData gameData, PendingMayAbility ability,
            ReturnMatchingPermanentsUnlessControllerPaysEffect effect, boolean paid) {
        if (!paid) {
            bounce(gameData, ability.sourceCard(), ability.targetCardId());
        }
        if (gameData.bounceUnlessPaysRemaining.isEmpty()) {
            return;
        }
        offerNext(gameData, effect, ability.sourceCard(), gameData.bounceUnlessPaysRemaining.removeFirst());
    }

    /**
     * Offers the pay-or-bounce prompt for {@code permanentId}, skipping ahead through the queue while
     * a permanent has left the battlefield (or has no controller left to decide).
     */
    private void offerNext(GameData gameData, ReturnMatchingPermanentsUnlessControllerPaysEffect effect,
            Card sourceCard, UUID permanentId) {
        UUID current = permanentId;
        while (true) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, current);
            UUID controllerId = permanent == null ? null : gameQueryService.findPermanentController(gameData, current);
            if (permanent != null && controllerId != null) {
                String prompt = "Pay " + effect.manaCost() + "? If you don't, " + permanent.getCard().getName()
                        + " is returned to its owner's hand. (" + sourceCard.getName() + ")";
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        sourceCard, controllerId, List.of(effect), prompt, current, effect.manaCost()));
                return;
            }
            if (gameData.bounceUnlessPaysRemaining.isEmpty()) {
                return;
            }
            current = gameData.bounceUnlessPaysRemaining.removeFirst();
        }
    }

    private void bounce(GameData gameData, Card sourceCard, UUID permanentId) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent == null) {
            return;
        }
        if (permanentRemovalService.removePermanentToHand(gameData, permanent)) {
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is returned to its owner's hand."));
            log.info("Game {} - {} returned to owner's hand by {}", gameData.id,
                    permanent.getCard().getName(), sourceCard.getName());
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /** Matching permanents in APNAP order so simultaneous decisions are offered in turn order. */
    private List<UUID> matchingPermanents(GameData gameData, ReturnMatchingPermanentsUnlessControllerPaysEffect e) {
        List<UUID> matching = new ArrayList<>();
        for (UUID playerId : apnapOrder(gameData)) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : List.copyOf(battlefield)) {
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, e.filter())) {
                    matching.add(permanent.getId());
                }
            }
        }
        return matching;
    }

    private static List<UUID> apnapOrder(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }
}
