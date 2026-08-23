package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
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
 * Destroys all matching permanents in the effect's scope, then resolves the optional
 * per-destroyed-count rider: the count of permanents actually destroyed is snapshotted onto a
 * derived stack entry's {@code eventValue} and the rider dispatches through its own handler
 * (the {@link DestroyTargetPermanentThenEffectHandler} pattern generalized to board wipes).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DestroyAllPermanentsEffectHandler implements NormalEffectHandlerBean {

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
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                // CR 608.2b: filters that ask about the source (e.g. "creatures blocking it") still
                // need it after a sacrifice cost removed it from the battlefield.
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                // X-relative filters ("each nonland permanent with mana value X or less") read the
                // X paid when the spell was cast (CR 601.2b).
                .withXValue(entry.getXValue());

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
                        && predicateEvaluationService.matchesPermanentPredicate(perm, e.filter(), filterContext)) {
                    toDestroy.add(perm);
                    controllerByPermanentId.put(perm.getId(), targetPlayerId);
                    manaValueByPermanentId.put(perm.getId(), perm.getCard().getManaValue());
                }
            }
        } else {
            gameData.forEachBattlefield((playerId, battlefield) -> {
                for (Permanent perm : battlefield) {
                    if (!sparedIds.contains(perm.getId())
                            && predicateEvaluationService.matchesPermanentPredicate(perm, e.filter(), filterContext)) {
                        toDestroy.add(perm);
                        controllerByPermanentId.put(perm.getId(), playerId);
                        manaValueByPermanentId.put(perm.getId(), perm.getCard().getManaValue());
                    }
                }
            });
        }

        List<Permanent> destroyed = destructionSupport.destroyBatchCollecting(
                gameData, toDestroy, entry.getCard().getName(), e.cannotBeRegenerated());
        entry.setEventCardIds(destroyed.stream().map(perm -> perm.getCard().getId()).toList());

        if (e.thenEffect() == null) {
            return;
        }

        // Controllers and mana values stay positionally aligned so per-permanent riders can pair them.
        List<UUID> destroyedControllerIds = new ArrayList<>();
        List<Integer> destroyedManaValues = new ArrayList<>();
        for (Permanent perm : destroyed) {
            UUID controllerId = controllerByPermanentId.get(perm.getId());
            if (controllerId == null) {
                continue;
            }
            destroyedControllerIds.add(controllerId);
            destroyedManaValues.add(manaValueByPermanentId.getOrDefault(perm.getId(), 0));
        }

        int destroyedCount = switch (e.destroyedCountScope()) {
            case ALL -> destroyed.size();
            case CONTROLLER -> (int) destroyedControllerIds.stream()
                    .filter(entry.getControllerId()::equals)
                    .count();
        };
        StackEntry thenEntry = new StackEntry(entry.getEntryType(), entry.getCard(), entry.getControllerId(),
                entry.getDescription(), List.of(e.thenEffect()), entry.getTargetId(), entry.getSourcePermanentId());
        thenEntry.setEventValue(destroyedCount);
        thenEntry.setEventPlayerIds(destroyedControllerIds);
        thenEntry.setEventManaValues(destroyedManaValues);
        thenEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

        EffectHandler handler = effectHandlerRegistry.getHandler(e.thenEffect());
        if (handler != null) {
            handler.resolve(gameData, thenEntry, e.thenEffect());
        } else {
            log.warn("Game {} - No handler for then-effect: {}", gameData.id, e.thenEffect().getClass().getSimpleName());
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
