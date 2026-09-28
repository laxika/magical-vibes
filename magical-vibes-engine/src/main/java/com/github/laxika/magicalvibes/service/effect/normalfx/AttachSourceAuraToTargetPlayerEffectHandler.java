package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachSourceAuraToTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachSourceAuraToTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        Permanent aura = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)
                || aura == null || !aura.getCard().isAura() || !aura.getCard().isEnchantPlayer()) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        if (!auraAttachmentService.canEnchantPlayer(gameData, aura.getCard(), controllerId, targetPlayerId)) {
            log.info("Game {} - {} cannot attach to player {}", gameData.id, aura.getCard().getName(), targetPlayerId);
            return;
        }

        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(targetPlayerId);
        aura.setTimestamp(gameData.nextTimestamp());
        gameLogService.append(gameData, GameLog.text(aura.getCard().getName() + " is now attached to "
                + gameData.playerIdToName.get(targetPlayerId) + "."));
        log.info("Game {} - {} attached to player {}", gameData.id, aura.getCard().getName(), targetPlayerId);
    }
}
