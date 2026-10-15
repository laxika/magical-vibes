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

    @Test
    void onlyTheRemainingLockedDoorCanBeChosen() {
        Permanent room = addRoom(player1);
        room.unlockRoomDoor(0);
        attackWithKeybearer();

        resolveCombat();
        harness.handlePermanentChosen(player1, room.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice doorChoice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(doorChoice.options()).hasSize(1);
        harness.handleListChoice(player1, doorChoice.options().getFirst());

        assertThat(room.isRoomFullyUnlocked()).isTrue();
    }

    @Test
    void removedTargetDoesNotUnlockAnotherRoom() {
        Permanent room = addRoom(player1);
        Permanent otherRoom = addRoom(player1);
        attackWithKeybearer();

        resolveCombat();
        harness.handlePermanentChosen(player1, room.getId());
        gd.playerBattlefields.get(player1.getId()).remove(room);
        gd.playerGraveyards.get(player1.getId()).add(room.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(otherRoom.isRoomDoorUnlocked(0)).isFalse();
        assertThat(otherRoom.isRoomDoorUnlocked(1)).isFalse();
    }

    @Test
    void abilityStillUnlocksADoorAfterKeybearerLeavesTheBattlefield() {
        Permanent room = addRoom(player1);
        attackWithKeybearer();

        resolveCombat();
        harness.handlePermanentChosen(player1, room.getId());
        Permanent keybearer = findPermanent(player1, "Ghostly Keybearer");
        gd.playerBattlefields.get(player1.getId()).remove(keybearer);
        gd.playerGraveyards.get(player1.getId()).add(keybearer.getCard());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice doorChoice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, doorChoice.options().getLast());

        assertThat(room.isRoomDoorUnlocked(0)).isFalse();
        assertThat(room.isRoomDoorUnlocked(1)).isTrue();
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
