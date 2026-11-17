package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.rules.AttachmentEvent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.duration.DurationClock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachSourceAuraToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DurationClock durationClock;
    private final AuraAttachmentService auraAttachmentService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachSourceAuraToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData,
                ((AttachSourceAuraToTargetCreatureEffect) effect).triggeringPermanent()
                        ? entry.getTriggeringPermanentId() : entry.getTargetId());
        if (target == null) {
            
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s attach ability fizzles (target creature no longer exists)."));
            log.info("Game {} - Attach source aura fizzles, target creature left battlefield", gameData.id);
            return;
        }

        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null) {
            
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s attach ability fizzles (Aura no longer on the battlefield)."));
            log.info("Game {} - Attach source aura fizzles, Aura left battlefield", gameData.id);
            return;
        }

        if (target.getId().equals(aura.getAttachedTo())) return;

        if (!auraAttachmentService.canEnchant(gameData, aura.getCard(), entry.getControllerId(), target)) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " can't be attached to " + target.getCard().getName() + "."));
            return;
        }

        durationClock.sourceUnattached(gameData, aura.getId());
        aura.setAttachedTo(target.getId());
        // CR 613.7e: an Aura receives a new timestamp each time it becomes attached.
        aura.setTimestamp(gameData.nextTimestamp());

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " is now attached to " + target.getCard().getName() + "."));
        log.info("Game {} - {} attached to {}", gameData.id, entry.getCard().getName(), target.getCard().getName());

        gameData.rulesEvents.occur(new AttachmentEvent.AuraAttached(aura, target.getId()));
    }
}
