package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMatchingPermanentsUnlessOwnerPaysEffect;
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
 * Resolves {@link ReturnMatchingPermanentsUnlessOwnerPaysEffect}: every permanent matching the
 * filter gets an independent pay-or-be-bounced decision made by its OWNER, offered one at a time
 * (remaining permanent ids live in {@link GameData#bounceUnlessPaysRemaining}) in APNAP order.
 *
 * <p>Cut the Tethers reads "return it to its owner's hand unless that player pays {3}" — "that
 * player" is the owner just named, not the controller. The two differ only once a permanent has
 * changed hands, and there the payer must be the same player whose hand would receive it, so the
 * owner is resolved with the engine's usual battlefield convention
 * ({@code stolenCreatures.getOrDefault(id, controllerId)}) that
 * {@code PermanentRemovalService.removePermanentToHand} also uses. Because the decision belongs to
 * the owner, the APNAP sequencing is by owner too (CR 101.4).
 *
 * <p>The prompt is always shown even when the owner has no floating mana — paying is a choice and
 * mana abilities may still be activated while the prompt is up; accepting without the mana falls
 * through to the bounce in {@code MayPenaltyChoiceHandlerService}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnMatchingPermanentsUnlessOwnerPaysEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnMatchingPermanentsUnlessOwnerPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnMatchingPermanentsUnlessOwnerPaysEffect) effect;
        List<UUID> matching = matchingPermanents(gameData, e);

        gameData.bounceUnlessPaysRemaining.clear();
        if (matching.isEmpty()) {
            return;
        }
        gameData.bounceUnlessPaysRemaining.addAll(matching.subList(1, matching.size()));
        offerNext(gameData, e, entry.getCard(), matching.getFirst());
    }

    /**
     * After a decision on one permanent: bounce it when its owner declined or couldn't pay, then
     * offer the next queued permanent. Called from {@code MayPenaltyChoiceHandlerService}.
     */
    public void afterPermanentDecision(GameData gameData, PendingMayAbility ability,
            ReturnMatchingPermanentsUnlessOwnerPaysEffect effect, boolean paid) {
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
     * a permanent has left the battlefield (or has no owner left to decide).
     */
    private void offerNext(GameData gameData, ReturnMatchingPermanentsUnlessOwnerPaysEffect effect,
            Card sourceCard, UUID permanentId) {
        UUID current = permanentId;
        while (true) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, current);
            UUID ownerId = permanent == null ? null : findOwner(gameData, current);
            if (permanent != null && ownerId != null) {
                String prompt = "Pay " + effect.manaCost() + "? If you don't, " + permanent.getCard().getName()
                        + " is returned to its owner's hand. (" + sourceCard.getName() + ")";
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        sourceCard, ownerId, List.of(effect), prompt, current, effect.manaCost()));
                return;
            }
            if (gameData.bounceUnlessPaysRemaining.isEmpty()) {
                return;
            }
            current = gameData.bounceUnlessPaysRemaining.removeFirst();
        }
    }

    /**
     * The owner of a permanent on the battlefield: whoever it was taken from if control has changed,
     * otherwise its current controller. Same convention as
     * {@code PermanentRemovalService.removePermanentToHand}, so the player offered the payment is
     * always the one whose hand would receive the permanent.
     */
    private UUID findOwner(GameData gameData, UUID permanentId) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, permanentId);
        return gameData.stolenCreatures.getOrDefault(permanentId, controllerId);
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

    /**
     * Matching permanents grouped by the player who decides for them — the OWNER — in APNAP order, so
     * simultaneous decisions are offered in turn order (CR 101.4). A permanent whose control has
     * changed is therefore offered with its owner's batch, not the batch of the player it sits with.
     */
    private List<UUID> matchingPermanents(GameData gameData, ReturnMatchingPermanentsUnlessOwnerPaysEffect e) {
        List<UUID> matched = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : List.copyOf(battlefield)) {
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, e.filter())) {
                    matched.add(permanent.getId());
                }
            }
        }

        List<UUID> ordered = new ArrayList<>();
        for (UUID playerId : apnapOrder(gameData)) {
            for (UUID permanentId : matched) {
                if (playerId.equals(findOwner(gameData, permanentId))) {
                    ordered.add(permanentId);
                }
            }
        }
        // An owner who is no longer in the game leaves its permanents unassigned above; keep them in
        // the queue so they are still bounced rather than silently skipped.
        for (UUID permanentId : matched) {
            if (!ordered.contains(permanentId)) {
                ordered.add(permanentId);
            }
        }
        return ordered;
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
