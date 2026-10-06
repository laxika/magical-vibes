package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeysToTheHouse.class, DazzlingTheaterPropRoom.class, Forest.class})
class KeysToTheHouseTest extends BaseCardTest {

    @Test
    void sacrificesAndSearchesForABasicLand() {
        harness.addToBattlefield(player1, new KeysToTheHouse());
        harness.setLibrary(player1, List.of(new Forest(), new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Keys to the House");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof KeysToTheHouse);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(1).allMatch(card -> card instanceof Forest);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Forest);
    }

    @Test
    void sacrificesAndTogglesAChosenDoorOfAControlledRoom() {
        Permanent keys = harness.addToBattlefieldAndReturn(player1, new KeysToTheHouse());
        Permanent room = harness.addToBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(keys), 1, null, room.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        harness.handleListChoice(player1, choice.options().stream()
                .filter(option -> option.contains("Door 1"))
                .findFirst().orElseThrow());

        assertThat(room.isRoomDoorUnlocked(0)).isTrue();
        assertThat(room.isRoomDoorUnlocked(1)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(keys);
    }

    @Test
    void cannotTargetAnOpponentControlledRoom() {
        harness.addToBattlefield(player1, new KeysToTheHouse());
        Permanent opponentRoom = harness.addToBattlefieldAndReturn(player2, new DazzlingTheaterPropRoom());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentRoom.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void locksAnUnlockedDoorWithoutChangingTheOtherDoor() {
        harness.addToBattlefield(player1, new KeysToTheHouse());
        Permanent room = harness.addToBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        room.unlockRoomDoor(0);
        room.unlockRoomDoor(1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, room.getId());
        harness.assertNotOnBattlefield(player1, "Keys to the House");
        harness.assertInGraveyard(player1, "Keys to the House");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        harness.handleListChoice(player1, choice.options().stream()
                .filter(option -> option.contains("Door 2"))
                .findFirst().orElseThrow());

        assertThat(room.isRoomDoorUnlocked(0)).isTrue();
        assertThat(room.isRoomDoorUnlocked(1)).isFalse();
    }

    @Test
    void unlocksTheSecondDoorOfAPartiallyUnlockedRoom() {
        harness.addToBattlefield(player1, new KeysToTheHouse());
        Permanent room = harness.addToBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        room.unlockRoomDoor(0);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, room.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        harness.handleListChoice(player1, choice.options().stream()
                .filter(option -> option.contains("Door 2"))
                .findFirst().orElseThrow());

        assertThat(room.isRoomDoorUnlocked(0)).isTrue();
        assertThat(room.isRoomDoorUnlocked(1)).isTrue();
    }

    @Test
    void cannotActivateRoomAbilityDuringUpkeep() {
        harness.addToBattlefield(player1, new KeysToTheHouse());
        Permanent room = harness.addToBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, room.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Keys to the House");
    }

    @Test
    void canSearchForLandDuringUpkeep() {
        harness.addToBattlefield(player1, new KeysToTheHouse());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertInGraveyard(player1, "Keys to the House");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void searchWithNoBasicLandsLeavesTheLibraryAndHandIntact() {
        harness.addToBattlefield(player1, new KeysToTheHouse());
        DazzlingTheaterPropRoom nonland = new DazzlingTheaterPropRoom();
        harness.setLibrary(player1, List.of(nonland));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Keys to the House");
    }

    @Test
    void cannotTargetANonRoomPermanent() {
        harness.addToBattlefield(player1, new KeysToTheHouse());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Keys to the House");
    }

    @Test
    void cannotActivateEitherAbilityWhileTapped() {
        Permanent keys = harness.addToBattlefieldAndReturn(player1, new KeysToTheHouse());
        Permanent room = harness.addToBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        keys.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, room.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Keys to the House");
    }
}
