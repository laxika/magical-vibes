package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesBlockingOrBlockedByTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves {@link ReturnCreaturesBlockingOrBlockedByTargetEffect}. */
@Component
@RequiredArgsConstructor
public class ReturnCreaturesBlockingOrBlockedByTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final BounceSupport bounceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnCreaturesBlockingOrBlockedByTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.targetsForEffect(effect).stream().findFirst().orElse(entry.getTargetId());
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        Set<UUID> combatOpponentIds = target.isBlocking()
                ? new LinkedHashSet<>(target.getBlockingTargetIds())
                : new LinkedHashSet<>();
        gameData.forEachBattlefield((playerId, battlefield) -> battlefield.stream()
                .filter(permanent -> permanent.isBlocking()
                        && permanent.getBlockingTargetIds().contains(targetId))
                .map(Permanent::getId)
                .forEach(combatOpponentIds::add));

        List<Permanent> toReturn = new ArrayList<>();
        for (UUID combatOpponentId : combatOpponentIds) {
            Permanent combatOpponent = gameQueryService.findPermanentById(gameData, combatOpponentId);
            if (combatOpponent != null && gameQueryService.isCreature(gameData, combatOpponent)) {
                toReturn.add(combatOpponent);
            }
        }

        // They leave as one event (CR 603.10a).
        bounceSupport.applyReturnPermanentsToHand(gameData, entry, toReturn);
    }
}
