package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlockImpostorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns another creature you control to its owner's hand")
    void etbReturnsAnotherCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlockImpostor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Flock Impostor");
    }

    @Test
    @DisplayName("ETB can choose no creature")
    void etbCanChooseNoCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlockImpostor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Flock Impostor");
    }

    @Test
    @DisplayName("ETB cannot target a creature an opponent controls")
    void etbCannotTargetOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlockImpostor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, bears.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }
}
