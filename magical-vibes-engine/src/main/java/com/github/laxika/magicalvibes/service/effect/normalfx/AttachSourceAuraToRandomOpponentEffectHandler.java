package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToRandomOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class AttachSourceAuraToRandomOpponentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachSourceAuraToRandomOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || !aura.getCard().isAura() || !aura.getCard().isEnchantPlayer()) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        UUID currentPlayerId = aura.getAttachedTo();
        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .filter(playerId -> !playerId.equals(currentPlayerId))
                .filter(playerId -> auraAttachmentService.canEnchantPlayer(
                        gameData, aura.getCard(), controllerId, playerId))
                .toList();
        if (opponents.isEmpty()) {
            return;
        }

        UUID chosenPlayerId = opponents.get(ThreadLocalRandom.current().nextInt(opponents.size()));
        gameData.expireFloatingEffectsForUnattachedSource(aura.getId());
        aura.setAttachedTo(chosenPlayerId);
        aura.setTimestamp(gameData.nextTimestamp());
        gameLogService.append(gameData, GameLog.text(aura.getCard().getName() + " is now attached to "
                + gameData.playerIdToName.get(chosenPlayerId) + "."));
    }
}
