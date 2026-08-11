package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
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
public class LivingWeaponEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LivingWeaponEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (LivingWeaponEffect) effect;
        List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), e.token(), entry.getCard().getSetCode());
        entry.getCreatedPermanentIds().addAll(createdIds);
        if (createdIds.isEmpty()) {
            return;
        }

        Permanent token = gameQueryService.findPermanentById(gameData, createdIds.getLast());
        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (token == null || equipment == null) {
            return;
        }

        gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
        equipment.setAttachedTo(token.getId());
        equipment.setTimestamp(gameData.nextTimestamp());
        gameLogService.append(gameData,
                GameLog.cardThen(entry.getCard(), " is now attached to " + token.getCard().getName() + "."));
        log.info("Game {} - {} attached to {} token via living weapon", gameData.id,
                entry.getCard().getName(), token.getCard().getName());
    }
}
