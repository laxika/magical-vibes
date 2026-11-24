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
import com.github.laxika.magicalvibes.model.rules.RoomEvent;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Asks the controller for a door of a Room they control among the Rooms the effect names: a locked one to unlock
 * (CR 709.5f), or with {@code lockOrUnlock} either one, an unlocked door being locked (CR 709.5g). Unlocking gives the
 * Room a new timestamp and raises the door-unlocked event, and the fully-unlocked event when it was the second door
 * (CR 709.5i).
 */
@Component
@RequiredArgsConstructor
public class UnlockControlledRoomDoorEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnlockControlledRoomDoorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UnlockControlledRoomDoorEffect unlock = (UnlockControlledRoomDoorEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<ChoiceContext.UnlockRoomDoorChoice.RoomDoor> choices = new ArrayList<>();
        for (Permanent room : subjectResolutionService.permanents(gameData, entry, effect, unlock.rooms())) {
            if (isRoom(room) && controllerId.equals(gameQueryService.findPermanentController(gameData, room.getId()))) {
                addDoorChoices(choices, room, roomIndex(gameData, controllerId, room), unlock.lockOrUnlock());
            }
        }
        if (choices.isEmpty()) {
            return;
        }

        ChoiceContext.UnlockRoomDoorChoice context = new ChoiceContext.UnlockRoomDoorChoice(
                entry.getCard(), controllerId, choices, unlock.lockOrUnlock());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, context,
                choices.stream().map(ChoiceContext.UnlockRoomDoorChoice.RoomDoor::label).toList(),
                entry.getCard().getName() + (unlock.lockOrUnlock()
                        ? " - Choose a door to lock or unlock."
                        : " - Choose a locked door of a Room you control.")));
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
                || choice.doorIndex() < 0 || choice.doorIndex() > 1
                || !context.lockOrUnlock() && room.isRoomDoorUnlocked(choice.doorIndex())) {
            throw new IllegalStateException("That Room door is no longer available");
        }

        boolean locks = room.isRoomDoorUnlocked(choice.doorIndex());
        if (locks) {
            room.lockRoomDoor(choice.doorIndex());
        } else {
            boolean wasFullyUnlocked = room.isRoomFullyUnlocked();
            room.unlockRoomDoor(choice.doorIndex());
            room.setTimestamp(gameData.nextTimestamp());
            gameData.rulesEvents.occur(new RoomEvent.DoorUnlocked(room, context.controllerId(), choice.doorIndex()));
            if (!wasFullyUnlocked && room.isRoomFullyUnlocked()) {
                gameData.rulesEvents.occur(new RoomEvent.FullyUnlocked(room, context.controllerId()));
            }
        }

        gameData.interaction.clearAwaitingInput();
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(context.controllerId())
                        + (locks ? " locks a door of " : " unlocks a door of "),
                room.getCard(), "."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private boolean isRoom(Permanent permanent) {
        return permanent.getCard().hasType(CardType.ENCHANTMENT)
                && permanent.getCard().getSubtypes().contains(CardSubtype.ROOM)
                && permanent.getCard().getRoomDoorManaCosts().size() == 2;
    }

    private void addDoorChoices(List<ChoiceContext.UnlockRoomDoorChoice.RoomDoor> choices, Permanent room,
                                int permanentIndex, boolean lockOrUnlock) {
        for (int doorIndex = 0; doorIndex < 2; doorIndex++) {
            boolean unlocked = room.isRoomDoorUnlocked(doorIndex);
            if (unlocked && !lockOrUnlock) {
                continue;
            }
            String action = !lockOrUnlock ? "" : unlocked ? "Lock " : "Unlock ";
            choices.add(new ChoiceContext.UnlockRoomDoorChoice.RoomDoor(
                    room.getId(), doorIndex,
                    action + FaceDownVisibility.publicName(room) + " (Room " + (permanentIndex + 1)
                            + ") - Door " + (doorIndex + 1)));
        }
    }

    private int roomIndex(GameData gameData, UUID controllerId, Permanent room) {
        return gameData.playerBattlefields.getOrDefault(controllerId, List.of()).indexOf(room);
    }
}
