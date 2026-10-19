package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var destroy = (DestroyTargetPermanentEffect) effect;
        if (destroy.tokenForController() == null && destroyEveryBoundGroupSimultaneously(gameData, entry, destroy)) {
            return;
        }
        List<UUID> effectTargets = entry.targetsForEffect(effect);
        UUID targetId = destroy.targetGroup() >= 0
                ? entry.targetsForGroup(destroy.targetGroup()).stream().findFirst().orElse(null)
                : effectTargets.isEmpty() ? entry.getTargetId() : effectTargets.getFirst();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) {
                    return;
                }

                // Capture the controller before destruction (needed for token creation)
                UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
                if (controllerId != null) {
                    entry.getRemovedPermanentControllers().put(target.getId(), controllerId);
                }

                entry.rememberLastKnownPermanentCard(target.getId(), target.getCard());
                entry.getLastKnownPermanentPowers().put(target.getId(),
                        gameQueryService.getEffectivePower(gameData, target));
                destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), destroy.cannotBeRegenerated());

                // Create token for the target's controller if specified
                if (destroy.tokenForController() != null && controllerId != null
                        && (!destroy.tokenOnlyForSourceController()
                        || controllerId.equals(entry.getControllerId()))) {
                    destructionSupport.createTokenForPlayer(gameData, controllerId, destroy.tokenForController(),
                            destroy.tokenCount(), entry.getCard().getName(), entry.getCard().getSetCode());
                }
    }

    /**
     * One destroy effect bound to several target groups ("destroy target artifact and target
     * enchantment") destroys all of its targets at the same time, so each dying permanent's leaves-
     * the-battlefield abilities see the others die with it. Returns {@code false} when the effect is
     * bound to at most one group.
     */
    private boolean destroyEveryBoundGroupSimultaneously(GameData gameData, StackEntry entry,
                                                          DestroyTargetPermanentEffect destroy) {
        Card targeting = entry.getTargetingCard();
        if (targeting == null || targeting.getEffectTargetIndex(destroy, 1) < 0) {
            return false;
        }
        Set<UUID> targetIds = new LinkedHashSet<>();
        for (int occurrence = 0; targeting.getEffectTargetIndex(destroy, occurrence) >= 0; occurrence++) {
            targetIds.addAll(entry.targetsForGroup(targeting.getEffectTargetIndex(destroy, occurrence)));
        }
        List<Permanent> toDestroy = new ArrayList<>();
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
            if (controllerId != null) {
                entry.getRemovedPermanentControllers().put(target.getId(), controllerId);
            }
            entry.rememberLastKnownPermanentCard(target.getId(), target.getCard());
            entry.getLastKnownPermanentPowers().put(target.getId(),
                    gameQueryService.getEffectivePower(gameData, target));
            toDestroy.add(target);
        }
        destructionSupport.destroyBatch(gameData, toDestroy, entry.getCard().getName(),
                destroy.cannotBeRegenerated());
        return true;
    }
}
