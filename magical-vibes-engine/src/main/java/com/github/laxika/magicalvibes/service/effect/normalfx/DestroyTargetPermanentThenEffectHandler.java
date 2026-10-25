package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLandDropEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveRiderForPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerMayPayManaOrLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Resolves the destroy-plus-value family via {@link DestroyTargetPermanentThenEffect}: destroy the
 * targeted permanent, then resolve an existing then-effect through its own handler.
 *
 * <p>The controller of the destroyed permanent and the requested last-known stat
 * ({@link com.github.laxika.magicalvibes.model.effect.EventStat}) are snapshotted <em>before</em>
 * destruction. The then-effect is then resolved against a derived stack entry whose controller is the
 * chosen {@link ThenEffectRecipient} and whose {@code eventValue} carries the snapshot, so the
 * then-effect's ordinary {@code CONTROLLER}-style recipient lands on the right player with the right
 * value — without any then-effect needing its own "target permanent's controller" variant.
 *
 * <p>When this effect is a step of the resolution and its then-effect is resolved by a handler marked
 * {@link AsksDeathReplacementsFirst} (a life loss), the then-effect becomes the next step of the same entry, a
 * {@link ResolveRiderForPlayerEffect} carrying that recipient and value; any other then-effect, or one dispatched
 * by another handler, resolves at once.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentThenEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameOutcomeService gameOutcomeService;
    private final ObjectProvider<EffectResolutionService> effectResolutionService;
    private final ResolveRiderForPlayerEffectHandler riderStepHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyTargetPermanentThenEffect) effect;
        // Whether this effect is the step the resolution loop is resolving on this entry (not one another
        // handler dispatches on an entry of its own), read before the destruction consumes the mark.
        boolean resolvesAsStep = gameData.stepDepartureEffect == effect && entry.getResolvingEffectIndex() >= 0;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            resolveForDepartedReferencedPermanent(gameData, entry, e);
            return;
        }

        // Snapshot everything read from the destroyed permanent BEFORE it leaves the battlefield.
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID targetOwnerId = gameData.defaultControllerOf(target.getId());
        boolean wasLand = e.stat() == EventStat.BASIC_LAND_SEARCH_COUNT
                && predicateEvaluationService.matchesPermanentPredicate(
                        gameData, target, new PermanentIsLandPredicate());
        int statValue = switch (e.stat()) {
            case NONE -> 0;
            case MANA_VALUE -> gameQueryService.getPermanentManaValue(target);
            case TOUGHNESS -> gameQueryService.getEffectiveToughness(gameData, target);
            case POWER -> gameQueryService.getPowerBasedDamage(gameData, target);
            case TOTAL_COUNTERS -> target.getTotalCounterCount();
            case BASIC_LAND_SEARCH_COUNT -> 0;
        };
        FilterContext conditionContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        boolean thenApplies = e.thenCondition() == null
                || predicateEvaluationService.matchesPermanentPredicate(target, e.thenCondition(), conditionContext);

        // Unless the card says "destroyed this way" or "dies this way", the then-effect happens regardless
        // of whether destruction succeeds (indestructible / regeneration).
        long previousGraveyardVersion = gameData.graveyardEntryVersion(target.getOriginalCard().getId());
        boolean destroyed = destructionSupport.tryDestroyAndLog(
                gameData, target, entry.getCard().getName(), e.cannotBeRegenerated());
        // If destruction was replaced, read the surviving permanent's current characteristics.
        if (gameQueryService.findPermanentById(gameData, target.getId()) != null) {
            statValue = switch (e.stat()) {
                case TOUGHNESS -> gameQueryService.getEffectiveToughness(gameData, target);
                case POWER -> gameQueryService.getPowerBasedDamage(gameData, target);
                case MANA_VALUE -> gameQueryService.getPermanentManaValue(target);
                default -> statValue;
            };
        }
        if (e.stat() == EventStat.BASIC_LAND_SEARCH_COUNT) {
            statValue = wasLand && destroyed ? 2 : 1;
        }

        boolean died = gameData.graveyardEntryVersion(target.getOriginalCard().getId()) > previousGraveyardVersion;
        if (!thenApplies || (e.requiresDestruction() && (!destroyed || e.requiresDeath() && !died))) {
            return;
        }

        resolveThenEffect(gameData, entry, e, statValue, targetControllerId, targetOwnerId,
                target.getOriginalCard().getId(), resolvesAsStep);
    }

    /**
     * An ability that merely references "that creature" without targeting it (Engulfing Slagwurm) still
     * resolves after the creature has left the battlefield: nothing is destroyed, but the then-effect
     * uses the creature's last known information.
     */
    private void resolveForDepartedReferencedPermanent(GameData gameData, StackEntry entry,
                                                       DestroyTargetPermanentThenEffect e) {
        UUID departedId = entry.getTargetId();
        if (!entry.isNonTargeting() || departedId == null || e.thenCondition() != null || e.requiresDestruction()) {
            return;
        }
        // Nothing is destroyed, so the then-effect is this handler's first change; it may change
        // the game before a death of its own, so it can't be stopped and resolved again.
        gameData.deathReplacementsCanRerunEffect = false;
        Card lastKnownCard = entry.lastKnownPermanentCard(departedId);
        Integer lastKnownToughness = entry.getLastKnownPermanentToughnesses().get(departedId);
        Integer lastKnownPower = entry.getLastKnownPermanentPowers().get(departedId);
        int statValue;
        switch (e.stat()) {
            case NONE -> statValue = 0;
            case MANA_VALUE -> {
                if (lastKnownCard == null) return;
                statValue = lastKnownCard.getManaValue();
            }
            case TOUGHNESS -> {
                if (lastKnownToughness == null) return;
                statValue = lastKnownToughness;
            }
            case POWER -> {
                if (lastKnownPower == null) return;
                statValue = Math.max(0, lastKnownPower);
            }
            default -> {
                return;
            }
        }
        UUID departedControllerId = entry.getRemovedPermanentControllers().get(departedId);
        UUID departedOwnerId = lastKnownCard == null ? null : lastKnownCard.getOwnerId();
        resolveThenEffect(gameData, entry, e, statValue, departedControllerId, departedOwnerId, null, false);
    }

    private void resolveThenEffect(GameData gameData, StackEntry entry, DestroyTargetPermanentThenEffect e,
                                   int statValue, UUID targetControllerId, UUID targetOwnerId,
                                   UUID targetCardId, boolean resolvesAsStep) {
        UUID thenControllerId = switch (e.recipient()) {
            case TARGET_CONTROLLER -> targetControllerId;
            case TARGET_OWNER -> targetOwnerId;
            case CONTROLLER, TARGET_CONTROLLER_AS_TARGET, TARGET_OWNER_AS_TARGET -> entry.getControllerId();
        };
        if (thenControllerId == null) {
            return;
        }

        // *_AS_TARGET retargets the rider at the destroyed permanent's controller/owner while the
        // caster stays the resolving controller (damage shields / "you control …" checks key on the source).
        UUID thenTargetId = switch (e.recipient()) {
            case TARGET_CONTROLLER_AS_TARGET -> targetControllerId;
            case TARGET_OWNER_AS_TARGET -> targetOwnerId;
            default -> entry.getTargetId();
        };
        CardEffect thenEffect = e.thenEffect();
        // CR 608.2c: the rider follows the destruction. As a step of its own on this entry it resolves in its own
        // window, so a question it asks before its first change (Lich's Duel Mastery's sacrifice for "its
        // controller loses 2 life") reruns only the rider, never the destruction.
        // A plain sequence with such a rider among its steps (Parting Thoughts' "you draw X cards and you lose X life")
        // becomes one rider step per instruction, in order.
        List<CardEffect> riderSteps = resolvesAsStep ? riderSteps(thenEffect) : List.of();
        if (!riderSteps.isEmpty()) {
            Map<UUID, UUID> removedControllers = targetControllerId != null && entry.getTargetId() != null
                    ? Map.of(entry.getTargetId(), targetControllerId) : Map.of();
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, riderSteps.stream()
                    .<CardEffect>map(rider -> new ResolveRiderForPlayerEffect(rider, thenControllerId, thenTargetId,
                            statValue, removedControllers,
                            rider instanceof SacrificePermanentsEffect && targetCardId != null
                                    ? List.of(targetCardId) : List.of()))
                    .toList());
            return;
        }
        StackEntry thenEntry = new StackEntry(entry.getEntryType(), entry.getCard(), thenControllerId,
                entry.getDescription(), List.of(thenEffect), thenTargetId, entry.getSourcePermanentId());
        thenEntry.setEventValue(statValue);
        if (targetControllerId != null && entry.getTargetId() != null) {
            thenEntry.getRemovedPermanentControllers().putIfAbsent(entry.getTargetId(), targetControllerId);
        }
        thenEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        thenEntry.setTriggeringPermanentId(entry.getTriggeringPermanentId());
        if (thenEffect instanceof SacrificePermanentsEffect && targetCardId != null) {
            thenEntry.setTargetCardIds(List.of(targetCardId));
        }

        if (thenEffect instanceof ConditionalEffect conditional) {
            if (!conditionEvaluationService.isMet(
                    gameData, conditional.condition(), ConditionContext.forStackEntry(thenEntry))) {
                return;
            }
            thenEffect = conditional.wrapped();
        }

        if (thenEffect instanceof MayEffect may && may.elseEffect() == null
                && may.choicePlayer() == MayChoicePlayer.CONTROLLER
                && may.wrapped() instanceof SearchLibraryEffect search
                && search.searchPlayer() == LibrarySearchPlayer.CONTROLLER) {
            var boundSearch = new SearchLibraryEffect(search.count(), search.filter(), search.destination(),
                    search.manaValueBound(), search.castFromGraveyardCount(), search.requireDifferentNames(),
                    search.grantHaste(), search.exileAtEndStep(), search.returnToHandAtEndStep(),
                    search.animateFound(), LibrarySearchPlayer.TRIGGERING_PERMANENT_CONTROLLER,
                    search.onlyIfSacrificed(), search.battlefieldIfChosenBeholdType(), search.shuffleAfterSelection(),
                    search.battlefieldCounter(), search.enterWithCounters(), search.topLibraryPosition(),
                    search.battlefieldIfOpponentControlsMoreLands(), search.topLibraryFractionDenominator());
            // The destroyed permanent's controller makes the choice by identity: the entry's triggering
            // permanent may be the source itself (an enters trigger), whose controller is the caster.
            var boundMay = MayEffect.forPlayer(boundSearch, may.prompt(), thenControllerId);
            entry.setTriggeringPermanentControllerId(thenControllerId);
            entry.setEventValue(statValue);
            entry.replaceEffectToResolve(entry.getResolvingEffectIndex(), boundMay);
            effectHandlerRegistry.getHandler(boundMay).resolve(gameData, entry, boundMay);
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        if (thenEffect instanceof SequenceEffect) {
            effectResolutionService.getObject().resolveEffects(gameData, thenEntry);
        } else {
            EffectHandler handler = effectHandlerRegistry.getHandler(thenEffect);
            if (handler != null) {
                handler.resolve(gameData, thenEntry, thenEffect);
            } else {
                log.warn("Game {} - No handler for then-effect: {}", gameData.id, thenEffect.getClass().getSimpleName());
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * The riders {@code thenEffect} resolves as steps of their own: itself when its handler is marked
     * ({@link ResolveRiderForPlayerEffectHandler#resolvesAsStep}), the steps of a plain, ordered sequence when one of
     * them is (the others being effects the resolution loop doesn't special-case, so each resolves the same as a rider
     * step), or none.
     */
    private List<CardEffect> riderSteps(CardEffect thenEffect) {
        if (riderStepHandler.resolvesAsStep(thenEffect)) {
            return List.of(thenEffect);
        }
        if (!(thenEffect instanceof SequenceEffect sequence) || sequence.simultaneous()
                || sequence.controllerDrawCount() != 0 || sequence.onlyIfSacrificed() || sequence.optionalTarget()
                || sequence.steps().stream().noneMatch(riderStepHandler::resolvesAsStep)
                || !sequence.steps().stream().allMatch(step -> riderStepHandler.resolvesAsStep(step)
                        || !(step instanceof MayEffect || step instanceof SequenceEffect
                        || step instanceof ConditionalEffect || step instanceof ConditionalReplacementEffect
                        || step instanceof MayPayManaEffect || step instanceof MayPayLandDropEffect
                        || step instanceof MayPayTapPermanentsEffect
                        || step instanceof TargetPlayerMayPayManaOrLifeEffect))) {
            return List.of();
        }
        return sequence.steps();
    }
}
