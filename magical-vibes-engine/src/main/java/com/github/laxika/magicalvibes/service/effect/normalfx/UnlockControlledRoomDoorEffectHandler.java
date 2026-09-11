package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UnlockControlledRoomDoorEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UnlockControlledRoomDoorEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnlockControlledRoomDoorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UnlockControlledRoomDoorEffect unlockEffect = (UnlockControlledRoomDoorEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<ChoiceContext.UnlockRoomDoorChoice.RoomDoor> choices = new ArrayList<>();
        if (unlockEffect.targeted()) {
            List<UUID> targetIds = entry.targetsForEffect(effect);
            UUID targetId = !targetIds.isEmpty() ? targetIds.getFirst() : entry.getTargetId();
            Permanent room = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
            if (room == null || !isRoom(room)
                    || !controllerId.equals(gameQueryService.findPermanentController(gameData, room.getId()))) {
                return;
            }
            addLockedDoorChoices(choices, room, roomIndex(gameData, controllerId, room));
        } else {
            List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(controllerId, List.of());
            for (int permanentIndex = 0; permanentIndex < battlefield.size(); permanentIndex++) {
                Permanent room = battlefield.get(permanentIndex);
                if (isRoom(room)) {
                    addLockedDoorChoices(choices, room, permanentIndex);
                }
            }
        }

        if (choices.isEmpty()) {
            return;
        }

        ChoiceContext.UnlockRoomDoorChoice context = new ChoiceContext.UnlockRoomDoorChoice(
                entry.getCard(), controllerId, choices);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, context,
                choices.stream().map(ChoiceContext.UnlockRoomDoorChoice.RoomDoor::label).toList(),
                entry.getCard().getName() + " - Choose a locked door of a Room you control."));
    }

    public void completeChoice(GameData gameData, String selectedLabel,
                               ChoiceContext.UnlockRoomDoorChoice context) {
        ChoiceContext.UnlockRoomDoorChoice.RoomDoor choice = context.choices().stream()
                .filter(candidate -> candidate.label().equals(selectedLabel))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Room door choice: " + selectedLabel));

        Permanent room = gameQueryService.findPermanentById(gameData, choice.roomPermanentId());
        if (room == null
                || !context.controllerId().equals(gameQueryService.findPermanentController(gameData, room.getId()))
                || !isRoom(room)
                || room.isRoomDoorUnlocked(choice.doorIndex())) {
            throw new IllegalStateException("That Room door is no longer available");
        }

        boolean wasFullyUnlocked = room.isRoomFullyUnlocked();
        room.unlockRoomDoor(choice.doorIndex());
        triggerCollectionService.checkSelfRoomDoorUnlockedTriggers(
                gameData, context.controllerId(), room, choice.doorIndex());
        if (!wasFullyUnlocked && room.isRoomFullyUnlocked()) {
            triggerCollectionService.checkAllyRoomFullyUnlockedTriggers(
                    gameData, context.controllerId(), room);
        }

        gameData.interaction.clearAwaitingInput();
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(context.controllerId()) + " unlocks a door of ",
                room.getCard(), "."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private boolean isRoom(Permanent permanent) {
        return permanent.getCard().hasType(CardType.ENCHANTMENT)
                && permanent.getCard().getSubtypes().contains(CardSubtype.ROOM)
                && permanent.getCard().getRoomDoorManaCosts().size() == 2;
    }

    private void addLockedDoorChoices(List<ChoiceContext.UnlockRoomDoorChoice.RoomDoor> choices,
                                      Permanent room, int permanentIndex) {
        for (int doorIndex = 0; doorIndex < 2; doorIndex++) {
            if (!room.isRoomDoorUnlocked(doorIndex)) {
                choices.add(new ChoiceContext.UnlockRoomDoorChoice.RoomDoor(
                        room.getId(), doorIndex,
                        room.getCard().getName() + " (Room " + (permanentIndex + 1)
                                + ") - Door " + (doorIndex + 1)));
            }
        }
    }

    private int roomIndex(GameData gameData, UUID controllerId, Permanent room) {
        return gameData.playerBattlefields.getOrDefault(controllerId, List.of()).indexOf(room);
    }
}
