package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBlast;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RushingTideZuberaTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards when it dies after being dealt four damage this turn")
    void drawsThreeCardsAfterFourDamage() {
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new RushingTideZubera());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new LightningBlast()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castInstant(player2, 0, zubera.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        harness.assertInGraveyard(player1, "Rushing-Tide Zubera");
    }

    @Test
    @DisplayName("Does not draw when it dies after being dealt less than four damage this turn")
    void doesNotDrawAfterLessThanFourDamage() {
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new RushingTideZubera());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new Shock(), new DiabolicEdict()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, zubera.getId());
        harness.passBothPriorities();

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        harness.assertInGraveyard(player1, "Rushing-Tide Zubera");
    }
}
