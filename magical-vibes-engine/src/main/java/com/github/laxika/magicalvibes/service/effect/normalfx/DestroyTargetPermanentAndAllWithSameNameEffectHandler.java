package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndAllWithSameNameEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentAndAllWithSameNameEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentAndAllWithSameNameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        var sameNameEffect = (DestroyTargetPermanentAndAllWithSameNameEffect) effect;
        String targetName = gameQueryService.getEffectiveName(gameData, target);
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

        List<Permanent> toDestroy = new ArrayList<>();
        if (targetName == null) {
            // A face-down permanent has no name (CR 708.2), so no other permanent shares it.
            toDestroy.add(target);
        } else {
            gameData.forEachBattlefield((playerId, battlefield) -> {
                for (Permanent permanent : battlefield) {
                    if (targetName.equals(gameQueryService.getEffectiveName(gameData, permanent))
                            && predicateEvaluationService.matchesPermanentPredicate(
                                    permanent, sameNameEffect.sameNamePredicate(), filterContext)) {
                        toDestroy.add(permanent);
                    }
                }
            });
        }

        destructionSupport.destroyBatch(gameData, toDestroy, entry.getCard().getName(), false);
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
