package com.github.laxika.magicalvibes.service.effect.normalfx;

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
public class DestroyTargetPermanentAndAllWithSameNameEffectHandler implements NormalEffectHandlerBean {

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
        String targetName = target.getCard().getName();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

        List<Permanent> toDestroy = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getName().equals(targetName)
                        && predicateEvaluationService.matchesPermanentPredicate(
                                permanent, sameNameEffect.sameNamePredicate(), filterContext)) {
                    toDestroy.add(permanent);
                }
            }
        });

        destructionSupport.destroyBatch(gameData, toDestroy, entry.getCard().getName(), false);
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
