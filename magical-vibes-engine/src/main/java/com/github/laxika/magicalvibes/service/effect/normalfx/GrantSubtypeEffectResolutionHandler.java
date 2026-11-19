package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves a one-shot {@link GrantSubtypeEffect}: the subtype is stored as a {@link Duration#PERMANENT} floating
 * layer-4 effect on each affected permanent that is still on the battlefield, stamped as it is made. {@code SELF}
 * affects the source, {@code TARGET} the targets, and {@code TRIGGERING_PERMANENT} the permanent the trigger event
 * was about ("that creature becomes a Vampire in addition to its other types"), which is logged.
 */
@Component
@RequiredArgsConstructor
public class GrantSubtypeEffectResolutionHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantSubtypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantSubtypeEffect grant = (GrantSubtypeEffect) effect;
        List<UUID> targetIds;
        switch (grant.scope()) {
            case SELF -> targetIds = entry.getSourcePermanentId() == null
                    ? List.of() : List.of(entry.getSourcePermanentId());
            case TRIGGERING_PERMANENT -> targetIds = entry.getTriggeringPermanentId() == null
                    ? List.of() : List.of(entry.getTriggeringPermanentId());
            case TARGET -> {
                targetIds = entry.targetsForEffect(grant);
                if (targetIds.isEmpty() && entry.getTargetId() != null) {
                    targetIds = List.of(entry.getTargetId());
                }
            }
            default -> {
                return;
            }
        }
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            gameData.addEffectOnPermanent(entry, grant, targetId, Duration.PERMANENT);
            if (grant.scope() == GrantScope.TRIGGERING_PERMANENT) {
                gameLogService.append(gameData, GameLog.builder().card(target.getCard())
                        .text(" becomes a " + grant.subtype().getDisplayName() + " in addition to its other types.")
                        .build());
            }
        }
    }
}
