package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokensAndAttachEquipmentSupport {

    private final GameQueryService gameQueryService;
    private final EquipSupport equipSupport;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final GameLogService gameLogService;

    public void begin(GameData gameData, Card sourceCard, UUID controllerId, List<UUID> tokenIds) {
        promptNext(gameData, new PermanentChoiceContext.CreateTokensAndAttachEquipment(
                sourceCard, controllerId, List.copyOf(tokenIds), 0, List.of()), false);
    }

    public void handleChoice(GameData gameData, UUID chosenId,
                             PermanentChoiceContext.CreateTokensAndAttachEquipment context) {
        List<UUID> chosenEquipmentIds = new ArrayList<>(context.chosenEquipmentIds());
        chosenEquipmentIds.add(chosenId);
        promptNext(gameData, new PermanentChoiceContext.CreateTokensAndAttachEquipment(
                context.sourceCard(), context.controllerId(), context.tokenIds(),
                context.tokenIndex() + 1, List.copyOf(chosenEquipmentIds)), true);
    }

    private void promptNext(GameData gameData,
                            PermanentChoiceContext.CreateTokensAndAttachEquipment context,
                            boolean resumeResolution) {
        int tokenIndex = context.tokenIndex();
        while (tokenIndex < context.tokenIds().size()) {
            Permanent token = gameQueryService.findPermanentById(gameData, context.tokenIds().get(tokenIndex));
            List<UUID> validEquipmentIds = token == null
                    ? List.of()
                    : validEquipmentIds(gameData, context, token);
            if (!validEquipmentIds.isEmpty()) {
                PermanentChoiceContext nextContext = new PermanentChoiceContext.CreateTokensAndAttachEquipment(
                        context.sourceCard(), context.controllerId(), context.tokenIds(),
                        tokenIndex, context.chosenEquipmentIds());
                gameData.interaction.setPermanentChoiceContext(nextContext);
                playerInputService.beginAnyTargetChoice(
                        gameData, context.controllerId(), validEquipmentIds, List.of(context.controllerId()),
                        context.sourceCard().getName() + " — Choose an Equipment for token "
                                + (tokenIndex + 1) + ", or skip.");
                return;
            }

            List<UUID> skipped = new ArrayList<>(context.chosenEquipmentIds());
            skipped.add(context.controllerId());
            context = new PermanentChoiceContext.CreateTokensAndAttachEquipment(
                    context.sourceCard(), context.controllerId(), context.tokenIds(),
                    tokenIndex + 1, List.copyOf(skipped));
            tokenIndex++;
        }

        attachChosenEquipment(gameData, context);
        if (resumeResolution) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }

    private List<UUID> validEquipmentIds(GameData gameData,
                                         PermanentChoiceContext.CreateTokensAndAttachEquipment context,
                                         Permanent token) {
        List<UUID> validIds = new ArrayList<>();
        for (Permanent equipment : gameData.playerBattlefields.getOrDefault(context.controllerId(), List.of())) {
            if (!equipment.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                    || context.chosenEquipmentIds().contains(equipment.getId())
                    || !equipSupport.canAttachEquipment(gameData, equipment, token)) {
                continue;
            }
            validIds.add(equipment.getId());
        }
        return validIds;
    }

    private void attachChosenEquipment(GameData gameData,
                                       PermanentChoiceContext.CreateTokensAndAttachEquipment context) {
        List<Permanent> equipmentPermanents = new ArrayList<>();
        List<Permanent> tokenPermanents = new ArrayList<>();
        for (int i = 0; i < context.tokenIds().size(); i++) {
            UUID equipmentId = context.chosenEquipmentIds().get(i);
            if (equipmentId.equals(context.controllerId())) {
                continue;
            }
            Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentId);
            Permanent token = gameQueryService.findPermanentById(gameData, context.tokenIds().get(i));
            UUID equipmentController = equipment == null
                    ? null
                    : gameQueryService.findPermanentController(gameData, equipment.getId());
            if (equipment == null || token == null
                    || !context.controllerId().equals(equipmentController)
                    || !equipSupport.canAttachEquipment(gameData, equipment, token)) {
                continue;
            }
            equipmentPermanents.add(equipment);
            tokenPermanents.add(token);
        }

        for (int i = 0; i < equipmentPermanents.size(); i++) {
            Permanent equipment = equipmentPermanents.get(i);
            Permanent token = tokenPermanents.get(i);
            UUID oldAttachedTo = equipment.getAttachedTo();
            gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
            equipment.setAttachedTo(token.getId());
            equipment.setTimestamp(gameData.nextTimestamp());
            equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, token.getId());
            gameLogService.append(gameData,
                    GameLog.cardTextCard(equipment.getCard(), " is now attached to ", token.getCard(), "."));
            log.info("Game {} - {} attached to {} via {}", gameData.id,
                    equipment.getCard().getName(), token.getCard().getName(), context.sourceCard().getName());
        }
    }
}
