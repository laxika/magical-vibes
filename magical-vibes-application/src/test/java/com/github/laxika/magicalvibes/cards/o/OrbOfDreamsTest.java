package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RootMaze;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrbOfDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures enter tapped for both players")
    void creaturesEnterTappedForBothPlayers() {
        harness.addToBattlefield(player1, new OrbOfDreams());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent ownCreature = findPermanent(player1, "Grizzly Bears");
        assertThat(ownCreature.isTapped()).isTrue();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent opponentCreature = findPermanent(player2, "Grizzly Bears");
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Lands enter tapped")
    void landsEnterTapped() {
        harness.addToBattlefield(player1, new OrbOfDreams());
        harness.setHand(player1, List.of(new Forest()));

        gs.playCard(gd, player1, 0, 0, null, null);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchantments enter tapped")
    void enchantmentsEnterTapped() {
        harness.addToBattlefield(player1, new OrbOfDreams());
        harness.setHand(player1, List.of(new RootMaze()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Root Maze").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Existing permanents are not tapped when Orb of Dreams enters")
    void existingPermanentsAreNotTappedWhenOrbEnters() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new OrbOfDreams());

        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }
}
