package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WaterspoutElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, it does not return creatures or skip a turn")
    void withoutKickerDoesNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaterspoutElemental()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Waterspout Elemental");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Kicker returns all other creatures, leaves itself and noncreatures, and skips a turn")
    void kickedEtbReturnsOtherCreaturesAndSkipsTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new WaterspoutElemental()));
        addBaseMana();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Waterspout Elemental");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
