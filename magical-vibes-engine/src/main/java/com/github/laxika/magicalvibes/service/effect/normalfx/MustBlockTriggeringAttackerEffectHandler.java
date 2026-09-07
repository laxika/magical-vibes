package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockTriggeringAttackerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MustBlockTriggeringAttackerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MustBlockTriggeringAttackerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent blocker = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        Permanent attacker = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (blocker == null || attacker == null) {
            return;
        }

        blocker.getMustBlockIds().add(attacker.getId());
        gameLogService.append(gameData, GameLog.cardTextCard(blocker.getCard(),
                " must block ", attacker.getCard(), " this turn if able."));
        log.info("Game {} - {} must block {} this turn if able", gameData.id,
                blocker.getCard().getName(), attacker.getCard().getName());
    }
}
