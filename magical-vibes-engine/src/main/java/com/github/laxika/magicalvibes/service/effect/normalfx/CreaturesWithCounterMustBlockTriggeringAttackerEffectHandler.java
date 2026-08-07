package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesWithCounterMustBlockTriggeringAttackerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreaturesWithCounterMustBlockTriggeringAttackerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreaturesWithCounterMustBlockTriggeringAttackerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreaturesWithCounterMustBlockTriggeringAttackerEffect blockEffect =
                (CreaturesWithCounterMustBlockTriggeringAttackerEffect) effect;

        UUID attackerId = entry.getTargetId();
        if (attackerId == null) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }

            for (Permanent permanent : new ArrayList<>(battlefield)) {
                if (permanent.getId().equals(attackerId)) {
                    continue;
                }
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                if (permanent.getCounterCount(blockEffect.counterType()) <= 0) {
                    continue;
                }

                permanent.getMustBlockIds().add(attackerId);
                gameLogService.append(gameData,
                        GameLog.textCardText("", permanent.getCard(), " blocks the attacking creature this turn if able."));
            }
        }
    }
}
