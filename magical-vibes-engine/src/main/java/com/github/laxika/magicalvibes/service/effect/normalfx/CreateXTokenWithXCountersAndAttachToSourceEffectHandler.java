package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersAndAttachToSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateXTokenWithXCountersAndAttachToSourceEffectHandler implements NormalEffectHandlerBean {

    private final CreateXTokenWithXCountersEffectHandler createTokenEffectHandler;
    private final GameQueryService gameQueryService;
    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateXTokenWithXCountersAndAttachToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var createEffect = (CreateXTokenWithXCountersAndAttachToSourceEffect) effect;
        int createdBefore = entry.getCreatedPermanentIds().size();
        createTokenEffectHandler.resolve(gameData, entry, createEffect.tokenEffect());
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        Permanent equipment = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (equipment == null) {
            return;
        }

        for (UUID createdId : entry.getCreatedPermanentIds().subList(
                createdBefore, entry.getCreatedPermanentIds().size())) {
            Permanent token = gameQueryService.findPermanentById(gameData, createdId);
            if (token == null || !equipSupport.attachEquipment(gameData, equipment, token)) {
                continue;
            }

            gameLogService.append(gameData,
                    GameLog.cardTextCard(equipment.getCard(), " is now attached to ", token.getCard(), "."));
            log.info("Game {} - {} attached to {} via {}", gameData.id,
                    equipment.getCard().getName(), token.getCard().getName(), entry.getCard().getName());
        }
    }
}
