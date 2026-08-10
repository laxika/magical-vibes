package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class JackalopeHerdTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell returns Jackalope Herd to its owner's hand")
    void ownSpellReturnsJackalopeHerdToHand() {
        harness.addToBattlefield(player1, new JackalopeHerd());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Jackalope Herd");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's spell does not return Jackalope Herd")
    void opponentSpellDoesNotReturnJackalopeHerd() {
        harness.addToBattlefield(player1, new JackalopeHerd());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);

        harness.assertOnBattlefield(player1, "Jackalope Herd");
    }
}
