package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MakeCreatureUnblockableEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeCreatureUnblockableEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var unblockable = (MakeCreatureUnblockableEffect) effect;

        // Multi-target: make each target in the group unblockable (e.g. Open into Wonder's
        // "X target creatures can't be blocked this turn").
        if (entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()) {
            for (UUID targetId : entry.getTargetIds()) {
                makeUnblockable(gameData, gameQueryService.findPermanentById(gameData, targetId));
            }
            return;
        }

        // Self-targeting triggers (e.g. Repartee) use sourcePermanentId; targetId may carry
        // separate context about the spell or player that caused the trigger.
        UUID targetId = unblockable.selfTargeting() && entry.getSourcePermanentId() != null
                ? entry.getSourcePermanentId()
                : entry.getTargetId();
        makeUnblockable(gameData, gameQueryService.findPermanentById(gameData, targetId));
    }

    private void makeUnblockable(GameData gameData, Permanent target) {
        if (target == null) {
            return;
        }

        target.setCantBeBlocked(true);

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " can't be blocked this turn."));

        log.info("Game {} - {} can't be blocked this turn", gameData.id, target.getCard().getName());
    }
}
