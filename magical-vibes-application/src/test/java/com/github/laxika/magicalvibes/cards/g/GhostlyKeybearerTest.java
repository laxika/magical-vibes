package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostlyKeybearer.class, DazzlingTheaterPropRoom.class})
class GhostlyKeybearerTest extends BaseCardTest {

    @Test
    void combatDamageTargetsAControlledRoomAndUnlocksAChosenDoor() {
        Permanent room = addRoom(player1);
        Permanent opponentRoom = addRoom(player2);
        attackWithKeybearer();

        resolveCombat();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).containsExactly(room.getId());
        assertThat(targetChoice.validPermanentIds()).doesNotContain(opponentRoom.getId());

        harness.handlePermanentChosen(player1, room.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice doorChoice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(doorChoice.options()).hasSize(2);
        harness.handleListChoice(player1, doorChoice.options().getFirst());

        assertThat(room.isRoomDoorUnlocked(0)).isTrue();
        assertThat(room.isRoomDoorUnlocked(1)).isFalse();
    }

    @Test
    void mayChooseNoRoomTarget() {
        Permanent room = addRoom(player1);
        attackWithKeybearer();

        resolveCombat();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(room.isRoomDoorUnlocked(0)).isFalse();
        assertThat(room.isRoomDoorUnlocked(1)).isFalse();
    }

    @Test
    void cannotTargetAnOpponentControlledRoom() {
        Permanent room = addRoom(player1);
        Permanent opponentRoom = addRoom(player2);
        attackWithKeybearer();

        resolveCombat();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentRoom.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, room.getId());
        harness.passBothPriorities();
        PendingInteraction.ColorChoice doorChoice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, doorChoice.options().getFirst());
    }

    @Test
    void targetingFullyUnlockedRoomDoesNothing() {
        Permanent room = addRoom(player1);
        room.unlockRoomDoor(0);
        room.unlockRoomDoor(1);
        attackWithKeybearer();

        resolveCombat();
        harness.handlePermanentChosen(player1, room.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(room.isRoomFullyUnlocked()).isTrue();
    }

    private Permanent addRoom(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new DazzlingTheaterPropRoom());
    }

    private void attackWithKeybearer() {
        Permanent attacker = addCreatureReady(player1, new GhostlyKeybearer());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
    }
}
