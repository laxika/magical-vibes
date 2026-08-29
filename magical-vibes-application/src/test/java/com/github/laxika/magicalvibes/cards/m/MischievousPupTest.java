package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MischievousPupTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns another permanent you control to its owner's hand")
    void etbReturnsAnotherPermanentYouControl() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        castPup(List.of(forest.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Mischievous Pup");
    }

    @Test
    @DisplayName("ETB can choose no permanent")
    void etbCanChooseNoPermanent() {
        castPup(List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mischievous Pup");
    }

    @Test
    @DisplayName("Cannot target an opponent's permanent")
    void cannotTargetOpponentsPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castPup(List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another permanent you control");
    }

    private void castPup(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new MischievousPup()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, targetIds);
    }
}
