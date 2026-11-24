package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Objects;

/**
 * "Unlock a locked door of a Room you control" (CR 709.5f): the controller chooses a locked door of one of the Rooms
 * {@code rooms} names that they control, and that Room gets its unlocked designation. With {@code lockOrUnlock}, "lock
 * or unlock a door of target Room you control": the controller chooses either door, and an unlocked one loses its
 * designation instead (CR 709.5g). Unlocking the second door fully unlocks the Room (CR 709.5i).
 *
 * @param rooms        the Rooms whose doors may be chosen: {@code target()} ("target Room you control", "up to one
 *                     target Room you control"), or the Rooms you control
 * @param lockOrUnlock whether an unlocked door may be chosen and locked
 */
public record UnlockControlledRoomDoorEffect(PermanentSubject rooms, boolean lockOrUnlock) implements CardEffect {

    private static final PermanentPredicate ROOM = new PermanentAllOfPredicate(List.of(
            new PermanentIsEnchantmentPredicate(), new PermanentHasSubtypePredicate(CardSubtype.ROOM)));

    public UnlockControlledRoomDoorEffect {
        Objects.requireNonNull(rooms, "rooms");
    }

    /** "Unlock a locked door of a Room you control." */
    public UnlockControlledRoomDoorEffect() {
        this(Subjects.controlledBy(Subjects.you(), ROOM), false);
    }

    /** "Unlock a locked door of up to one target Room you control." */
    public static UnlockControlledRoomDoorEffect targetRoom() {
        return new UnlockControlledRoomDoorEffect(Subjects.target(), false);
    }

    /** "Lock or unlock a door of target Room you control." */
    public static UnlockControlledRoomDoorEffect lockOrUnlockTargetRoom() {
        return new UnlockControlledRoomDoorEffect(Subjects.target(), true);
    }

    @Override
    public TargetSpec targetSpec() {
        if (!rooms.declaresTarget()) {
            return TargetSpec.NONE;
        }
        return TargetSpec.benign(TargetPredicates.permanent(), new PermanentAllOfPredicate(List.of(
                ROOM, new PermanentControlledBySourceControllerPredicate())));
    }
}
