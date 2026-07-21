package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantAdditionalBlockToTargetUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantAdditionalBlockToTargetUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantAdditionalBlockToTargetUntilEndOfTurnEffect) effect;

        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue; // Partially resolves — skip removed targets.
            }
            target.setAdditionalBlocksUntilEndOfTurn(
                    target.getAdditionalBlocksUntilEndOfTurn() + grant.additionalBlocks());

            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                    .card(target.getCard())
                    .text(String.format(" can block %d additional creature(s) this turn.", grant.additionalBlocks()))
                    .build());

            log.info("Game {} - {} can block {} additional creature(s) this turn",
                    gameData.id, target.getCard().getName(), grant.additionalBlocks());
        }
    }
}
