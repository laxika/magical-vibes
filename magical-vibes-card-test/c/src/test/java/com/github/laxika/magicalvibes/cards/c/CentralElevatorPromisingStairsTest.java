package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BottomlessPoolLockerRoom;
import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.g.GrandEntrywayElegantRotunda;
import com.github.laxika.magicalvibes.cards.g.GetOut;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        CentralElevatorPromisingStairs.class,
        BottomlessPoolLockerRoom.class,
        DazzlingTheaterPropRoom.class,
        GrandEntrywayElegantRotunda.class,
        GetOut.class
})
class CentralElevatorPromisingStairsTest extends BaseCardTest {

    @Test
    void centralElevatorExcludesRoomsSharingAnUnlockedDoorName() {
        castRoom(new BottomlessPoolLockerRoom(), 0, 1);
        harness.passBothPriorities();
        Card excludedRoom = new BottomlessPoolLockerRoom();
        Card eligibleRoom = new GrandEntrywayElegantRotunda();
        harness.setLibrary(player1, List.of(excludedRoom, eligibleRoom));

        castRoom(new CentralElevatorPromisingStairs(), 0, 4);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(eligibleRoom);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(excludedRoom);
        assertThat(gd.playerDecks.get(player1.getId())).contains(excludedRoom);
    }

    @Test
    void promisingStairsWinsWithEightDifferentUnlockedDoorNames() {
        Permanent centralElevator = castRoom(new CentralElevatorPromisingStairs(), 1, 3);
        centralElevator.unlockRoomDoor(0);
        addFullyUnlockedRoom(new BottomlessPoolLockerRoom());
        addFullyUnlockedRoom(new DazzlingTheaterPropRoom());
        addFullyUnlockedRoom(new GrandEntrywayElegantRotunda());
        harness.setLibrary(player1, List.of(new GrandEntrywayElegantRotunda()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void promisingStairsDoesNotTriggerWhileItsDoorIsLocked() {
        harness.addToBattlefieldAndReturn(player1, new CentralElevatorPromisingStairs());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void promisingStairsSurveilsWithoutEnoughNamesToWin() {
        castRoom(new CentralElevatorPromisingStairs(), 1, 3);
        Card topCard = new GrandEntrywayElegantRotunda();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    void promisingStairsDoesNotCountLockedDoorsOrDuplicateNames() {
        castRoom(new CentralElevatorPromisingStairs(), 1, 3);
        addFullyUnlockedRoom(new BottomlessPoolLockerRoom());
        addFullyUnlockedRoom(new DazzlingTheaterPropRoom());
        addFullyUnlockedRoom(new GrandEntrywayElegantRotunda());
        addFullyUnlockedRoom(new BottomlessPoolLockerRoom());
        harness.setLibrary(player1, List.of(new GrandEntrywayElegantRotunda()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    void promisingStairsStillSurveilsAfterItsSourceLeavesTheBattlefield() {
        Permanent room = castRoom(new CentralElevatorPromisingStairs(), 1, 3);
        Card topCard = new GrandEntrywayElegantRotunda();
        harness.setLibrary(player1, List.of(topCard));
        advanceToUpkeep(player1);

        harness.setHand(player1, List.of(new GetOut()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castModalInstant(player1, 0, 1, List.of(room.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(room.getOriginalCard().getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    void centralElevatorCanFindAnotherCopyOfAFullyLockedRoom() {
        harness.addToBattlefieldAndReturn(player1, new BottomlessPoolLockerRoom());
        Card eligibleRoom = new BottomlessPoolLockerRoom();
        harness.setLibrary(player1, List.of(eligibleRoom));

        castRoom(new CentralElevatorPromisingStairs(), 0, 4);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(eligibleRoom);
    }

    @Test
    void promisingStairsWinsEvenWithAnEmptyLibrary() {
        Permanent room = castRoom(new CentralElevatorPromisingStairs(), 1, 3);
        room.unlockRoomDoor(0);
        addFullyUnlockedRoom(new BottomlessPoolLockerRoom());
        addFullyUnlockedRoom(new DazzlingTheaterPropRoom());
        addFullyUnlockedRoom(new GrandEntrywayElegantRotunda());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void centralElevatorSearchesWhenUnlockedAfterCastingPromisingStairs() {
        castRoom(new CentralElevatorPromisingStairs(), 1, 3);
        Card eligibleRoom = new GrandEntrywayElegantRotunda();
        harness.setLibrary(player1, List.of(eligibleRoom));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.unlockRoomDoor(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(eligibleRoom);
    }

    private Permanent castRoom(Card room, int doorIndex, int manaAmount) {
        harness.setHand(player1, List.of(room));
        harness.addMana(player1, ManaColor.BLUE, manaAmount);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ROOM))
                .reduce((first, second) -> second)
                .orElseThrow();
    }

    private Permanent addFullyUnlockedRoom(Card room) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, room);
        permanent.unlockRoomDoor(0);
        permanent.unlockRoomDoor(1);
        return permanent;
    }
}
