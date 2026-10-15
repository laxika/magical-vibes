package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernalSovereign.class, Forest.class, GrizzlyBears.class})
class InfernalSovereignTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and loses 1 life when its controller plays a land")
    void triggersOnLandPlay() {
        harness.addToBattlefield(player1, new InfernalSovereign());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Draws a card and loses 1 life when its controller casts a spell")
    void triggersOnSpellCast() {
        harness.addToBattlefield(player1, new InfernalSovereign());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Skips its controller's draw step")
    void skipsControllerDrawStep() {
        harness.addToBattlefield(player1, new InfernalSovereign());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
    }

    @Test
    @DisplayName("Skips the entire draw step, including its priority window")
    void skipsEntireDrawStep() {
        harness.addToBattlefield(player1, new InfernalSovereign());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);

        harness.withAutoStop(TurnStep.DRAW, () ->
                harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities));

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Does not trigger when an opponent plays a land or casts a spell")
    void ignoresOpponentActions() {
        harness.addToBattlefield(player1, new InfernalSovereign());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player2, 0);
        assertThat(gd.stack).isEmpty();
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger for a land put onto the battlefield without being played")
    void ignoresLandEnteringWithoutBeingPlayed() {
        harness.addToBattlefield(player1, new InfernalSovereign());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A land-play trigger still draws and loses life after its source leaves")
    void landTriggerResolvesWithoutSource() {
        var sovereign = harness.addToBattlefieldAndReturn(player1, new InfernalSovereign());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(sovereign);

        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not skip the opponent's draw step")
    void opponentDrawsNormally() {
        harness.addToBattlefield(player1, new InfernalSovereign());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);

        harness.passUntil(TurnStep.DRAW);

        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting Sovereign does not trigger its own ability before it enters")
    void doesNotTriggerForItsOwnCast() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new InfernalSovereign()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each Sovereign triggers independently for the same land play")
    void multipleSovereignsEachTrigger() {
        harness.addToBattlefield(player1, new InfernalSovereign());
        harness.addToBattlefield(player1, new InfernalSovereign());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
