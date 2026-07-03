package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsAndGainLifePerDestroyedEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DestroyAllPermanentsAndGainLifePerDestroyedEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardService graveyardService;
    private final LifeSupport lifeSupport;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyAllPermanentsAndGainLifePerDestroyedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyAllPermanentsAndGainLifePerDestroyedEffect) effect;
        List<Permanent> toDestroy = new ArrayList<>();
                FilterContext filterContext = FilterContext.of(gameData)
                        .withSourceCardId(entry.getCard().getId())
                        .withSourceControllerId(entry.getControllerId());

                gameData.forEachBattlefield((playerId, battlefield) -> {
                    for (Permanent perm : battlefield) {
                        if (predicateEvaluationService.matchesPermanentPredicate(perm, e.filter(), filterContext)) {
                            toDestroy.add(perm);
                        }
                    }
                });

                // Snapshot indestructible before any removals
                Set<Permanent> indestructible = new HashSet<>();
                for (Permanent perm : toDestroy) {
                    if (gameQueryService.hasKeyword(gameData, perm, Keyword.INDESTRUCTIBLE)) {
                        indestructible.add(perm);
                    }
                }

                // Destroy and count
                int destroyedCount = 0;
                String sourceName = entry.getCard().getName();
                for (Permanent perm : toDestroy) {
                    if (indestructible.contains(perm)) {
                        gameBroadcastService.logAndBroadcast(gameData, perm.getCard().getName() + " is indestructible.");
                        continue;
                    }
                    if (graveyardService.tryRegenerate(gameData, perm)) {
                        continue;
                    }
                    permanentRemovalService.removePermanentToGraveyard(gameData, perm);
                    gameBroadcastService.logAndBroadcast(gameData, perm.getCard().getName() + " is destroyed.");
                    log.info("Game {} - {} is destroyed by {}", gameData.id, perm.getCard().getName(), sourceName);
                    destroyedCount++;
                }

                // Gain life for each destroyed permanent
                if (destroyedCount > 0) {
                    int totalLife = destroyedCount * e.lifePerDestroyed();
                    lifeSupport.applyGainLife(gameData, entry.getControllerId(), totalLife, sourceName);
                }
    }
}
