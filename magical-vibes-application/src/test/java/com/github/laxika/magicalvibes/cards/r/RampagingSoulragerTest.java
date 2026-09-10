package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RampagingSoulrager.class, DazzlingTheaterPropRoom.class})
class RampagingSoulragerTest extends BaseCardTest {

    @Test
    void getsPlusThreePowerWhenYouControlTwoUnlockedRoomDoors() {
        Permanent soulrager = harness.addToBattlefieldAndReturn(player1, new RampagingSoulrager());
        Permanent room = harness.addToBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());

        assertThat(gqs.getEffectivePower(gd, soulrager)).isEqualTo(1);
        room.unlockRoomDoor(0);
        assertThat(gqs.getEffectivePower(gd, soulrager)).isEqualTo(1);

        room.unlockRoomDoor(1);

        assertThat(gqs.getEffectivePower(gd, soulrager)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, soulrager)).isEqualTo(4);
    }

    @Test
    void doesNotCountDoorsOnRoomsAnOpponentControls() {
        Permanent soulrager = harness.addToBattlefieldAndReturn(player1, new RampagingSoulrager());
        Permanent opponentRoom = harness.addToBattlefieldAndReturn(player2, new DazzlingTheaterPropRoom());
        opponentRoom.unlockRoomDoor(0);
        opponentRoom.unlockRoomDoor(1);

        assertThat(gqs.getEffectivePower(gd, soulrager)).isEqualTo(1);
    }

    @Test
    void countsMatchingDoorNamesOnDifferentRoomsSeparately() {
        Permanent soulrager = harness.addToBattlefieldAndReturn(player1, new RampagingSoulrager());
        Permanent firstRoom = harness.addToBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        Permanent secondRoom = harness.addToBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        firstRoom.unlockRoomDoor(0);
        secondRoom.unlockRoomDoor(0);

        assertThat(gqs.getEffectivePower(gd, soulrager)).isEqualTo(4);
    }
}
