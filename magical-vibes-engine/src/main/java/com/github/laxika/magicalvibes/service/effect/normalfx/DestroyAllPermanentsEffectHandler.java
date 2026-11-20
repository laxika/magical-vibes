package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachDestroyedPermanentControllerGainsLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Destroys all matching permanents in the effect's scope as one event and records the step on the entry's resolution
 * scratchpad: the departure of every permanent it was told to destroy, so {@code StepKind.DESTROY}'s outcome is those
 * actually destroyed (wherever a replacement put them; indestructible and regenerated ones were not) with their
 * controllers as they were destroyed (CR 608.2h), and a later step counts those that died, or only some of them, from
 * the departures ({@code AmountThisWay}). It records even when nothing was destroyed.
 *
 * <p>A per-destroyed-permanent rider ({@code thenEffect}) is resolved after the destruction with the count of
 * permanents destroyed, their controllers and mana values stamped onto the entry's event values. When this effect is a
 * step of the resolution, the rider becomes the next step of the same entry; dispatched by another handler, it
 * resolves at once on a derived stack entry carrying those values.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DestroyAllPermanentsEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final DestructionSupport destructionSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyAllPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyAllPermanentsEffect) effect;
        entry.recordDestroyDepartures(List.of());
        // Whether this effect is the step the resolution loop is resolving on this entry (not one another
        // handler dispatches on an entry of its own), read before the destruction consumes the mark.
        boolean resolvesAsStep = gameData.stepDepartureEffect == effect;
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                // CR 608.2b: filters that ask about the source (e.g. "creatures blocking it") still
                // need it after a sacrifice cost removed it from the battlefield.
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                // X-relative filters ("each nonland permanent with mana value X or less") read the
                // X paid when the spell was cast (CR 601.2b).
                .withXValue(entry.getXValue());
        // "With that spell's mana value" (Celestial Kirin) reads the triggering spell as the ability resolves.
        PermanentPredicate filter = AmountEvaluationService.bindTriggeringSpellManaValue(gameData, e.filter(), entry);

        // "Destroy all other creatures" (Martial Coup): spare permanents this same resolution just
        // created, so the Soldier tokens made moments earlier survive the wipe.
        List<UUID> sparedIds = e.sparesPermanentsCreatedThisResolution()
                ? entry.getCreatedPermanentIds()
                : List.of();

        List<Permanent> toDestroy = new ArrayList<>();
        // Controllers are captured before destruction — afterwards the permanents are gone and their
        // controller can no longer be looked up (needed by per-permanent-controller riders).
        Map<UUID, UUID> controllerByPermanentId = new HashMap<>();
        Map<UUID, Integer> manaValueByPermanentId = new HashMap<>();
        if (e.scope() == EachPermanentScope.TARGET_PLAYER) {
            UUID targetPlayerId = entry.getTargetId();
            if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
                return;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
            if (battlefield == null) {
                return;
            }
            for (Permanent perm : List.copyOf(battlefield)) {
                if (!sparedIds.contains(perm.getId())
                        && predicateEvaluationService.matchesPermanentPredicate(perm, filter, filterContext)) {
                    toDestroy.add(perm);
                    controllerByPermanentId.put(perm.getId(), targetPlayerId);
                    manaValueByPermanentId.put(perm.getId(), perm.getCard().getManaValue());
                }
            }
        } else {
            gameData.forEachBattlefield((playerId, battlefield) -> {
                for (Permanent perm : battlefield) {
                    if (!sparedIds.contains(perm.getId())
                            && predicateEvaluationService.matchesPermanentPredicate(perm, filter, filterContext)) {
                        toDestroy.add(perm);
                        controllerByPermanentId.put(perm.getId(), playerId);
                        manaValueByPermanentId.put(perm.getId(), perm.getCard().getManaValue());
                    }
                }
            });
        }

        DestructionSupport.BatchOutcome outcome = destructionSupport.destroyBatchWithOutcome(
                gameData, toDestroy, entry.getCard().getName(), e.cannotBeRegenerated());
        entry.recordDestroyDepartures(outcome.results());
        entry.setEventCardIds(outcome.died().stream().map(perm -> perm.getCard().getId()).toList());
        // A permanent a replacement exiled or moved elsewhere instead of its graveyard was still destroyed
        // this way (Fumigate and Death Begets Life rulings).
        List<Permanent> destroyed = outcome.destroyed();

        if (e.thenEffect() == null) {
            return;
        }

        // Controllers and mana values stay positionally aligned so per-permanent riders can pair them.
        List<UUID> destroyedControllerIds = new ArrayList<>();
        List<Integer> destroyedManaValues = new ArrayList<>();
        // "The controller of each of those artifacts gains life" (Seeds of Innocence) covers every
        // artifact the instruction applied to, including indestructible ones that survived.
        List<Permanent> riderPermanents =
                e.thenEffect() instanceof EachDestroyedPermanentControllerGainsLifeEqualToManaValueEffect
                        ? toDestroy : destroyed;
        for (Permanent perm : riderPermanents) {
            UUID controllerId = controllerByPermanentId.get(perm.getId());
            if (controllerId == null) {
                continue;
            }
            destroyedControllerIds.add(controllerId);
            destroyedManaValues.add(manaValueByPermanentId.getOrDefault(perm.getId(), 0));
        }

        int destroyedCount = destroyed.size();
        // CR 608.2c: the rider follows the destruction. As a step of its own on this entry it resolves in
        // its own window, so a question it asks before its first change (Lich's Duel Mastery's sacrifice for
        // Deadly Tempest's life loss) reruns only the rider, never the destruction.
        if (resolvesAsStep && entry.getResolvingEffectIndex() >= 0) {
            stampRiderValues(entry, destroyedCount, destroyedControllerIds, destroyedManaValues);
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(e.thenEffect()));
            return;
        }

        StackEntry thenEntry = StackEntry.derivedFrom(entry, entry.getCard(), entry.getControllerId(),
                entry.getDescription(), List.of(e.thenEffect()), entry.getTargetId(), entry.getSourcePermanentId());
        stampRiderValues(thenEntry, destroyedCount, destroyedControllerIds, destroyedManaValues);
        thenEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

        EffectHandler handler = effectHandlerRegistry.getHandler(e.thenEffect());
        if (handler != null) {
            handler.resolve(gameData, thenEntry, e.thenEffect());
        } else {
            log.warn("Game {} - No handler for then-effect: {}", gameData.id, e.thenEffect().getClass().getSimpleName());
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    private static void stampRiderValues(StackEntry riderEntry, int destroyedCount, List<UUID> destroyedControllerIds,
                                         List<Integer> destroyedManaValues) {
        riderEntry.setEventValue(destroyedCount);
        riderEntry.setEventPlayerIds(destroyedControllerIds);
        riderEntry.setEventManaValues(destroyedManaValues);
    }
}
