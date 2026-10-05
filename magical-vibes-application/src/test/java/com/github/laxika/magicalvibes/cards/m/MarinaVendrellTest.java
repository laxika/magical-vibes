package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarinaVendrell.class, DazzlingTheaterPropRoom.class, GloriousAnthem.class,
        GrizzlyBears.class, Plains.class, Shock.class})
class MarinaVendrellTest extends BaseCardTest {

    @Test
    void entersAndPutsEnchantmentsAmongTopSevenIntoHand() {
        Card enchantment1 = new GloriousAnthem();
        Card creature1 = new GrizzlyBears();
        Card enchantment2 = new GloriousAnthem();
        Card land = new Plains();
        Card instant = new Shock();
        Card creature2 = new GrizzlyBears();
        Card instant2 = new Shock();
        Card enchantmentBelowTopSeven = new GloriousAnthem();
        harness.setLibrary(player1, List.of(
                enchantment1, creature1, enchantment2, land,
                instant, creature2, instant2, enchantmentBelowTopSeven));
        harness.castFromHand(player1, new MarinaVendrell(), "{W}{U}{B}{R}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment1, enchantment2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(enchantmentBelowTopSeven);
        assertThat(gd.playerDecks.get(player1.getId()))
                .contains(creature1, land, instant, creature2, instant2, enchantmentBelowTopSeven);
    }

    @Test
    void activatedAbilityUnlocksAChosenLockedDoor() {
        Permanent marina = addCreatureReady(player1, new MarinaVendrell());
        Permanent room = harness.addToBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        activateAtRoom(marina, room);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).hasSize(2);
        harness.handleListChoice(player1, choice.options().stream()
                .filter(option -> option.contains("Door 1"))
                .findFirst().orElseThrow());

        assertThat(room.isRoomDoorUnlocked(0)).isTrue();
        assertThat(room.isRoomDoorUnlocked(1)).isFalse();
    }

    @Test
    void activatedAbilityLocksAChosenUnlockedDoor() {
        Permanent marina = addCreatureReady(player1, new MarinaVendrell());
        Permanent room = harness.addToBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        room.unlockRoomDoor(0);
        room.unlockRoomDoor(1);
        activateAtRoom(marina, room);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).allMatch(option -> option.startsWith("Lock "));
        harness.handleListChoice(player1, choice.options().stream()
                .filter(option -> option.contains("Door 1"))
                .findFirst().orElseThrow());

        assertThat(room.isRoomDoorUnlocked(0)).isFalse();
        assertThat(room.isRoomDoorUnlocked(1)).isTrue();
    }

    @Test
    void activatedAbilityCannotTargetAnOpponentControlledRoom() {
        Permanent marina = addCreatureReady(player1, new MarinaVendrell());
        Permanent opponentRoom = harness.addToBattlefieldAndReturn(player2, new DazzlingTheaterPropRoom());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentRoom.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(marina.isTapped()).isFalse();
    }

    @Test
    void shortLibraryPutsRoomIntoHandAndReturnsNonEnchantments() {
        Card room = new DazzlingTheaterPropRoom();
        Card land = new Plains();
        Card creature = new MarinaVendrell();
        harness.setLibrary(player1, List.of(land, room, creature));
        harness.castFromHand(player1, new MarinaVendrell(), "{W}{U}{B}{R}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(room);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotPreventEntering() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new MarinaVendrell(), "{W}{U}{B}{R}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void allSevenEnchantmentsMustGoIntoHand() {
        List<Card> rooms = java.util.stream.IntStream.range(0, 7)
                .mapToObj(i -> (Card) new DazzlingTheaterPropRoom()).toList();
        harness.setLibrary(player1, rooms);
        harness.castFromHand(player1, new MarinaVendrell(), "{W}{U}{B}{R}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(rooms);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityCannotBeActivatedOutsideMainPhase() {
        Permanent marina = addCreatureReady(player1, new MarinaVendrell());
        Permanent room = harness.addToBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, room.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(marina.isTapped()).isFalse();
    }

    @Test
    void abilityOffersLockAndUnlockForPartiallyUnlockedRoom() {
        Permanent marina = addCreatureReady(player1, new MarinaVendrell());
        Permanent room = harness.addToBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        room.unlockRoomDoor(0);
        activateAtRoom(marina, room);

        assertThat(marina.isTapped()).isTrue();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).hasSize(2);
        assertThat(choice.options()).anyMatch(option -> option.startsWith("Lock ") && option.contains("Door 1"));
        harness.handleListChoice(player1, choice.options().stream()
                .filter(option -> option.startsWith("Unlock ") && option.contains("Door 2"))
                .findFirst().orElseThrow());

        assertThat(room.isRoomFullyUnlocked()).isTrue();
    }

    @Test
    void abilityCannotTargetANonRoomPermanent() {
        Permanent marina = addCreatureReady(player1, new MarinaVendrell());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(marina.isTapped()).isFalse();
    }

    @Test
    void abilityCannotBeActivatedWhileASpellIsOnTheStack() {
        Permanent marina = addCreatureReady(player1, new MarinaVendrell());
        Permanent room = harness.addToBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MarinaVendrell(), "{W}{U}{B}{R}{G}");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, room.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(marina.isTapped()).isFalse();
    }

    private void activateAtRoom(Permanent marina, Permanent room) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int marinaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(marina);
        harness.activateAbility(player1, marinaIndex, 0, null, room.getId());
        harness.passBothPriorities();
    }
}
