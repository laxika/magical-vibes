package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HallowedBurial.class, GrizzlyBears.class, LlanowarElves.class, Forest.class})
class HallowedBurialTest extends BaseCardTest {

    @Test
    @DisplayName("Puts all creatures on the bottom of their owners' libraries")
    void bottomsAllCreatures() {
        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        Forest player1LibraryCard = new Forest();
        Forest player2LibraryCard = new Forest();
        harness.addToBattlefield(player1, bears);
        harness.addToBattlefield(player2, elves);
        harness.setLibrary(player1, List.of(player1LibraryCard));
        harness.setLibrary(player2, List.of(player2LibraryCard));

        harness.castFromHand(player1, new HallowedBurial(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");

        // Cards go to the bottom of their owners' libraries, not the graveyard
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(player1LibraryCard, bears);
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactly(player2LibraryCard, elves);
    }

    @Test
    @DisplayName("Indestructible does not save a creature from Hallowed Burial")
    void indestructibleDoesNotSave() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.castFromHand(player1, new HallowedBurial(), "{3}{W}{W}");
        harness.passBothPriorities();

        // Not a destroy effect — indestructible is irrelevant
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Leaves noncreature permanents on the battlefield")
    void leavesNoncreaturePermanents() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new HallowedBurial(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Puts a controlled creature on the bottom of its owner's library")
    void usesCreatureOwnerLibrary() {
        GrizzlyBears opponentOwnedBears = new GrizzlyBears();
        opponentOwnedBears.setOwnerId(player2.getId());
        Forest player1LibraryCard = new Forest();
        Forest player2LibraryCard = new Forest();
        harness.addToBattlefield(player1, opponentOwnedBears);
        harness.setLibrary(player1, List.of(player1LibraryCard));
        harness.setLibrary(player2, List.of(player2LibraryCard));

        harness.castFromHand(player1, new HallowedBurial(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(player1LibraryCard);
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactly(player2LibraryCard, opponentOwnedBears);
    }

    @Test
    @DisplayName("The owner chooses the order when multiple creatures go to their library")
    void ownerChoosesBottomOrder() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new HallowedBurial(), "{3}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput())
                .as("The owner must be offered a choice of the two creatures' bottom order")
                .isTrue();
    }

    @Test
    @DisplayName("Shroud does not protect a creature from this untargeted spell")
    void shroudDoesNotSave() {
        GrizzlyBears card = new GrizzlyBears();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, card);
        bears.getGrantedKeywords().add(Keyword.SHROUD);
        Forest libraryCard = new Forest();
        harness.setLibrary(player2, List.of(libraryCard));

        harness.castFromHand(player1, new HallowedBurial(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard, card);
    }

    @Test
    @DisplayName("Does nothing when no creatures are on the battlefield")
    void doesNothingWhenNoCreatures() {
        harness.castFromHand(player1, new HallowedBurial(), "{3}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hallowed Burial");
    }
}
