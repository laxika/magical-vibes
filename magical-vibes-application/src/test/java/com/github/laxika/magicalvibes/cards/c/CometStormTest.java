package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CometStormTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to its single target without multikicker")
    void dealsDamageToSingleTarget() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CometStorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstantForX(player1, 0, 3, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals the full X damage to every multikicked target")
    void dealsFullDamageToEachTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CometStorm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantForX(player1, 0, 2, List.of(bears.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Rejects choosing the same target more than once")
    void rejectsDuplicateTargets() {
        harness.setHand(player1, List.of(new CometStorm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantForX(
                player1, 0, 2, List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
    }
}
