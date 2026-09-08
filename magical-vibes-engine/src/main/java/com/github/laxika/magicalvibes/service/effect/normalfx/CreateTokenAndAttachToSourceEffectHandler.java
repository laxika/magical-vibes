package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAndAttachToSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenAndAttachToSourceEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GameQueryService gameQueryService;
    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenAndAttachToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var createEffect = (CreateTokenAndAttachToSourceEffect) effect;
        int createdBefore = entry.getCreatedPermanentIds().size();
        createTokenEffectHandler.resolve(gameData, entry, createEffect.token());
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        UUID sourceId = entry.getSourcePermanentId();
        Permanent source = sourceId == null ? null : gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            return;
        }

        for (UUID createdId : entry.getCreatedPermanentIds().subList(
                createdBefore, entry.getCreatedPermanentIds().size())) {
            Permanent token = gameQueryService.findPermanentById(gameData, createdId);
            if (token == null || !equipSupport.attachEquipment(gameData, token, source)) {
                continue;
            }

            gameLogService.append(gameData,
                    GameLog.cardTextCard(token.getCard(), " is now attached to ", source.getCard(), "."));
            log.info("Game {} - {} attached to {} via {}", gameData.id,
                    token.getCard().getName(), source.getCard().getName(), entry.getCard().getName());
        }
    }
}
