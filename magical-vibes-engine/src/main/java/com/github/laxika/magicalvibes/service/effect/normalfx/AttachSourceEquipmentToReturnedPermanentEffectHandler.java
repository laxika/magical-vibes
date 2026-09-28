package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToReturnedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttachSourceEquipmentToReturnedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachSourceEquipmentToReturnedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent returned = findPermanentByCardId(gameData, returnedCardId(entry));
        Permanent equipment = entry.getSourcePermanentId() == null
                ? equipSupport.findEquipmentByCardId(gameData, entry.getCard().getId())
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (returned == null || equipment == null || !equipSupport.attachEquipment(gameData, equipment, returned)) {
            return;
        }

        gameLogService.append(gameData,
                GameLog.cardThen(entry.getCard(), " is now attached to " + returned.getCard().getName() + "."));
    }

    private UUID returnedCardId(StackEntry entry) {
        if (entry.getTargetId() != null) {
            return entry.getTargetId();
        }
        return entry.getTargetCardIds() == null ? null : entry.getTargetCardIds().stream().findFirst().orElse(null);
    }

    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        if (cardId == null) {
            return null;
        }
        return gameData.playerBattlefields.values().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(java.util.Collection::stream)
                .filter(permanent -> cardId.equals(permanent.getCard().getId())
                        || (permanent.getOriginalCard() != null
                        && cardId.equals(permanent.getOriginalCard().getId())))
                .findFirst()
                .orElse(null);
    }
}
