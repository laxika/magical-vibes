package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachSourceAuraToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachSourceAuraToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            String logEntry = entry.getCard().getName() + "'s attach ability fizzles (target creature no longer exists).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), "'s attach ability fizzles (target creature no longer exists)."));
            log.info("Game {} - Attach source aura fizzles, target creature left battlefield", gameData.id);
            return;
        }

        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null) {
            String logEntry = entry.getCard().getName() + "'s attach ability fizzles (Aura no longer on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), "'s attach ability fizzles (Aura no longer on the battlefield)."));
            log.info("Game {} - Attach source aura fizzles, Aura left battlefield", gameData.id);
            return;
        }

        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(target.getId());
        // CR 613.7e: an Aura receives a new timestamp each time it becomes attached.
        aura.setTimestamp(gameData.nextTimestamp());

        String logEntry = entry.getCard().getName() + " is now attached to " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), "'s attach ability fizzles (target creature no longer exists)."));
        log.info("Game {} - {} attached to {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
    }
}
