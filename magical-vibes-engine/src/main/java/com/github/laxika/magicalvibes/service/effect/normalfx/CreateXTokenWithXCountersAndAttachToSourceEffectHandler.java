package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersAndAttachToSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateXTokenWithXCountersAndAttachToSourceEffectHandler implements NormalEffectHandlerBean {

    private final CreateXTokenWithXCountersEffectHandler createTokenEffectHandler;
    private final GameQueryService gameQueryService;
    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

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

        List<UUID> createdIds = List.copyOf(entry.getCreatedPermanentIds().subList(
                createdBefore, entry.getCreatedPermanentIds().size()));
        if (createdIds.size() > 1) {
            // The controller chooses which of several created tokens the equipment attaches to.
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), createdIds,
                    new PermanentChoiceContext.AttachEquipmentToCreature(equipment.getId(), entry.getControllerId()),
                    "Choose a token to attach " + equipment.getCard().getName() + " to.");
            return;
        }

        for (UUID createdId : createdIds) {
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
