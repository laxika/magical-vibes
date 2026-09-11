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
import com.github.laxika.magicalvibes.model.effect.LockOrUnlockTargetRoomDoorEffect;
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
public class LockOrUnlockTargetRoomDoorEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LockOrUnlockTargetRoomDoorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> targetIds = entry.targetsForEffect(effect);
        UUID targetId = !targetIds.isEmpty() ? targetIds.getFirst() : entry.getTargetId();
        Permanent room = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (room == null || !isRoom(room)
                || !controllerId.equals(gameQueryService.findPermanentController(gameData, room.getId()))) {
            return;
        }

        List<ChoiceContext.LockOrUnlockRoomDoorChoice.RoomDoor> choices = new ArrayList<>();
        addDoorChoices(choices, room, roomIndex(gameData, controllerId, room));
        ChoiceContext.LockOrUnlockRoomDoorChoice context = new ChoiceContext.LockOrUnlockRoomDoorChoice(
                entry.getCard(), controllerId, choices);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, context,
                choices.stream().map(ChoiceContext.LockOrUnlockRoomDoorChoice.RoomDoor::label).toList(),
                entry.getCard().getName() + " - Choose a door to lock or unlock."));
    }

    public void completeChoice(GameData gameData, String selectedLabel,
                               ChoiceContext.LockOrUnlockRoomDoorChoice context) {
        ChoiceContext.LockOrUnlockRoomDoorChoice.RoomDoor choice = context.choices().stream()
                .filter(candidate -> candidate.label().equals(selectedLabel))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Room door choice: " + selectedLabel));

        Permanent room = gameQueryService.findPermanentById(gameData, choice.roomPermanentId());
        if (room == null
                || !context.controllerId().equals(gameQueryService.findPermanentController(gameData, room.getId()))
                || !isRoom(room)
                || choice.doorIndex() < 0 || choice.doorIndex() > 1) {
            throw new IllegalStateException("That Room door is no longer available");
        }

        boolean wasUnlocked = room.isRoomDoorUnlocked(choice.doorIndex());
        boolean wasFullyUnlocked = room.isRoomFullyUnlocked();
        if (wasUnlocked) {
            room.lockRoomDoor(choice.doorIndex());
        } else {
            room.unlockRoomDoor(choice.doorIndex());
            triggerCollectionService.checkSelfRoomDoorUnlockedTriggers(
                    gameData, context.controllerId(), room, choice.doorIndex());
            if (!wasFullyUnlocked && room.isRoomFullyUnlocked()) {
                triggerCollectionService.checkAllyRoomFullyUnlockedTriggers(
                        gameData, context.controllerId(), room);
            }
        }

        gameData.interaction.clearAwaitingInput();
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(context.controllerId())
                        + (wasUnlocked ? " locks a door of " : " unlocks a door of "),
                room.getCard(), "."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private boolean isRoom(Permanent permanent) {
        return permanent.getCard().hasType(CardType.ENCHANTMENT)
                && permanent.getCard().getSubtypes().contains(CardSubtype.ROOM)
                && permanent.getCard().getRoomDoorManaCosts().size() == 2;
    }

    private void addDoorChoices(List<ChoiceContext.LockOrUnlockRoomDoorChoice.RoomDoor> choices,
                               Permanent room, int permanentIndex) {
        for (int doorIndex = 0; doorIndex < 2; doorIndex++) {
            String action = room.isRoomDoorUnlocked(doorIndex) ? "Lock " : "Unlock ";
            choices.add(new ChoiceContext.LockOrUnlockRoomDoorChoice.RoomDoor(
                    room.getId(), doorIndex,
                    action + room.getCard().getName() + " (Room " + (permanentIndex + 1)
                            + ") - Door " + (doorIndex + 1)));
        }
    }

    private int roomIndex(GameData gameData, UUID controllerId, Permanent room) {
        return gameData.playerBattlefields.getOrDefault(controllerId, List.of()).indexOf(room);
    }
}
