package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentFromTargetPermanentsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnattachEquipmentFromTargetPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final UnattachTriggerSupport unattachTriggerSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnattachEquipmentFromTargetPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.getTargetIds();
        if (targetIds == null || targetIds.isEmpty()) {
            targetIds = entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
        }
        if (targetIds.isEmpty()) {
            return;
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            gameData.forEachPermanent((playerId, p) -> {
                if (targetId.equals(p.getAttachedTo())
                        && p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                    unattachTriggerSupport.triggerDestroyOnUnattachIfNeeded(gameData, p, p.getAttachedTo());
                    p.setAttachedTo(null);
                    gameData.expireFloatingEffectsForUnattachedSource(p.getId());
                    
                    gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" unattaches ").card(p.getCard()).text(" from ").card(target.getCard()).text(".").build());
                    log.info("Game {} - {} unattaches {} from {}", gameData.id, entry.getCard().getName(),
                            p.getCard().getName(), target.getCard().getName());
                }
            });
        }
    }
}
