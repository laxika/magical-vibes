package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentsWithinTotalManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentsWithinTotalManaValueEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentsWithinTotalManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var destroy = (DestroyTargetPermanentsWithinTotalManaValueEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int manaValueLimit = Math.max(0, amountEvaluationService.evaluate(gameData,
                destroy.totalManaValueLimit(), AmountContext.forStackEntry(entry, source)));

        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        List<Permanent> toDestroy = new ArrayList<>();
        Map<UUID, UUID> controllerByPermanentId = new HashMap<>();
        HashSet<UUID> seenTargetIds = new HashSet<>();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withXValue(entry.getXValue());
        int selectedManaValue = 0;
        for (UUID targetId : targetIds) {
            if (!seenTargetIds.add(targetId)) {
                continue;
            }
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !predicateEvaluationService.matchesPermanentPredicate(
                    target, destroy.filter(), filterContext)) {
                continue;
            }
            int targetManaValue = target.getCard().getManaValue();
            if (selectedManaValue + targetManaValue > manaValueLimit) {
                continue;
            }
            selectedManaValue += targetManaValue;
            toDestroy.add(target);
            UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (controllerId != null) {
                controllerByPermanentId.put(target.getId(), controllerId);
            }
        }

        List<Permanent> actuallyDestroyed = destructionSupport.destroyBatchCollecting(
                gameData, toDestroy, entry.getCard().getName(), false);
        List<UUID> destroyedControllerIds = new ArrayList<>();
        List<UUID> destroyedNontokenControllerIds = new ArrayList<>();
        for (Permanent permanent : actuallyDestroyed) {
            UUID controllerId = controllerByPermanentId.get(permanent.getId());
            if (controllerId != null) {
                destroyedControllerIds.add(controllerId);
                if (!permanent.getCard().isToken()) {
                    destroyedNontokenControllerIds.add(controllerId);
                }
            }
        }
        entry.setEventValue(actuallyDestroyed.size());
        entry.setEventPlayerIds(destroyedControllerIds);
        entry.setEventNontokenPlayerIds(destroyedNontokenControllerIds);
    }
}
