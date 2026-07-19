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
import java.util.List;
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
    private final GameQueryService gameQueryService;
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
                .withSourceControllerId(entry.getControllerId());

        // "Destroy all other creatures" (Martial Coup): spare permanents this same resolution just
        // created, so the Soldier tokens made moments earlier survive the wipe.
        List<UUID> sparedIds = e.sparesPermanentsCreatedThisResolution()
                ? entry.getCreatedPermanentIds()
                : List.of();

        List<Permanent> toDestroy = new ArrayList<>();
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
                }
            }
        } else {
            gameData.forEachBattlefield((playerId, battlefield) -> {
                for (Permanent perm : battlefield) {
                    if (!sparedIds.contains(perm.getId())
                            && predicateEvaluationService.matchesPermanentPredicate(perm, e.filter(), filterContext)) {
                        toDestroy.add(perm);
                    }
                }
            });
        }

        int destroyedCount = destructionSupport.destroyBatch(gameData, toDestroy, entry.getCard().getName(), e.cannotBeRegenerated());

        if (e.thenEffect() == null) {
            return;
        }

        StackEntry thenEntry = new StackEntry(entry.getEntryType(), entry.getCard(), entry.getControllerId(),
                entry.getDescription(), List.of(e.thenEffect()), entry.getTargetId(), entry.getSourcePermanentId());
        thenEntry.setEventValue(destroyedCount);
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
