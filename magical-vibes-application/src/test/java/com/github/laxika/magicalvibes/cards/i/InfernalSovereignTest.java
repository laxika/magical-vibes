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
}
