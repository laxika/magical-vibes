package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PlayersLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link ReturnToHandEffect}. The permanents are the effect's subject, found by
 * {@link SubjectResolutionService}. A target or the triggering permanent is returned one at a time with the optional
 * controller rider (Vapor Snag's life loss, Call to Heel's draw) and the last-known card recorded; the source is
 * returned as itself; every other subject's permanents leave as one simultaneous batch. The subjectless
 * {@link ReturnToHandEffect#selfSpell()} returns the resolving spell, and a target suspended card returns from exile.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final BounceSupport bounceSupport;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnToHandEffect) effect;
        if (e.returnsResolvingSpell()) {
            resolveSelfSpell(gameData, entry);
            return;
        }
        if (e.orSuspendedCard() && entry.getTargetZone() == Zone.EXILE) {
            resolveSuspendedCard(gameData, entry);
            return;
        }
        switch (e.subject()) {
            case PermanentSubject.Target ignored -> resolveTarget(gameData, entry, e);
            case PermanentSubject.Triggering ignored -> {
                for (UUID permanentId : subjectResolutionService.permanentIds(gameData, entry, e, e.subject())) {
                    bounceTarget(gameData, entry, e, permanentId);
                }
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
            case PermanentSubject.Source ignored -> bounceSupport.applyReturnSelfToHand(gameData, entry,
                    subjectResolutionService.permanent(gameData, entry, e, e.subject()));
            default -> resolveBatch(gameData, entry, e);
        }
    }

    /**
     * The resolving spell returns itself to its owner's hand instead of going to the graveyard.
     * Normally this is just a marker read once the entry finishes resolving, but an effect that
     * paused for player input earlier in the same resolution (Petals of Insight's "you may"
     * prompt) has already had its spell disposition applied, so the card is fetched back out of
     * the graveyard here.
     */
    private void resolveSelfSpell(GameData gameData, StackEntry entry) {
        entry.setReturnToHandAfterResolving(true);
        if (entry.getCard() == null) {
            return;
        }
        UUID ownerId = entry.getOwnerId();
        List<Card> graveyard = gameData.playerGraveyards.get(ownerId);
        if (graveyard != null && graveyard.remove(entry.getPhysicalCard())) {
            gameData.addCardToHand(ownerId, entry.getPhysicalCard());
        }
    }

    /**
     * Each target of this effect (its target group's slice for an effect bound through
     * {@code target(...).addEffect(...)}, e.g. Leave's "any number of target permanents you own", else the entry's
     * targets) is returned on its own; none chosen returns nothing. With {@link ReturnToHandEffect#ofChosenCreatureType()}
     * only a creature of the type chosen while casting returns.
     */
    private void resolveTarget(GameData gameData, StackEntry entry, ReturnToHandEffect e) {
        PermanentPredicate chosenTypeFilter = null;
        FilterContext filterContext = null;
        if (e.ofChosenCreatureType()) {
            if (entry.getChosenCreatureType() == null) {
                return;
            }
            chosenTypeFilter = new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentHasSubtypePredicate(entry.getChosenCreatureType())));
            filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard().getId())
                    .withSourceControllerId(entry.getControllerId())
                    .withXValue(entry.getXValue());
        }
        for (UUID targetId : subjectResolutionService.permanentIds(gameData, entry, e, e.subject())) {
            if (chosenTypeFilter != null) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null || !predicateEvaluationService.matchesPermanentPredicate(
                        target, chosenTypeFilter, filterContext)) {
                    continue;
                }
            }
            bounceTarget(gameData, entry, e, targetId);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        if (e.lifeLoss() > 0) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    private void resolveSuspendedCard(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        var exiled = gameData.findExiledCard(targetId);
        if (exiled == null || !RemoveTimeCounterFromExiledCardEffectHandler.isSuspended(gameData, exiled)) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " fizzles (target card is no longer suspended)."));
            return;
        }

        gameData.removeFromExile(targetId);
        gameData.suspendedSpellExiles.removeIf(suspended -> suspended.cardId().equals(targetId));
        gameData.addCardToHand(exiled.ownerId(), exiled.card());
        gameLogService.append(gameData, GameLog.textCardText(
                entry.getDescription() + " returns ", exiled.card(), " from exile to its owner's hand."));
    }

    private void bounceTarget(GameData gameData, StackEntry entry, ReturnToHandEffect e, UUID targetId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        UUID controllerId = (e.lifeLoss() > 0 || e.drawCount() > 0)
                ? gameQueryService.findPermanentController(gameData, target.getId())
                : null;

        UUID returnedControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        boolean returned = permanentRemovalService.removePermanentToHand(gameData, target);
        if (returned) {
            entry.rememberRemovedPermanentController(target.getId(), returnedControllerId);
            entry.rememberLastKnownPermanentCard(target.getId(), target.getCard());
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is returned to its owner's hand."));
            log.info("Game {} - {} returned to owner's hand by {}", gameData.id, target.getCard().getName(), entry.getCard().getName());
        }

        if (controllerId != null && e.drawCount() > 0) {
            playerInteractionSupport.applyDrawCards(gameData, controllerId, e.drawCount());
        }

        if (controllerId != null && e.lifeLoss() > 0) {
            // CR 608.2c: "its controller loses N life" follows the bounce, which can take away a replacement the
            // Lich's Duel Mastery sacrifice replacing the loss would have. As a step of its own, that sacrifice asks
            // its CR 616.1 questions without bouncing again.
            if (entry.getResolvingEffectIndex() >= 0) {
                entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                        List.of(new PlayersLoseLifeEffect(Map.of(controllerId, e.lifeLoss()))));
            } else {
                lifeSupport.applyLifeLoss(gameData, controllerId, e.lifeLoss(), entry.getCard().getName());
            }
        }
    }

    /**
     * Every permanent the subject names leaves as one simultaneous batch; then the optional follow-up resolves when
     * enough of the controller's nontoken permanents returned (Flood of Tears).
     */
    private void resolveBatch(GameData gameData, StackEntry entry, ReturnToHandEffect e) {
        List<Permanent> toReturn = subjectResolutionService.permanents(gameData, entry, e, e.subject());
        int returnedControlledNontokens = bounceAll(gameData, entry, toReturn);
        if (e.thenEffect() != null && returnedControlledNontokens >= e.minimumControlledNontokenCount()) {
            int effectIndex = entry.getEffectsToResolve().indexOf(e);
            if (effectIndex >= 0) {
                entry.insertEffectsToResolve(effectIndex + 1, List.of(e.thenEffect()));
            }
        }
    }

    private int bounceAll(GameData gameData, StackEntry entry, List<Permanent> toReturn) {
        return bounceSupport.applyReturnPermanentsToHand(gameData, entry, toReturn);
    }
}
