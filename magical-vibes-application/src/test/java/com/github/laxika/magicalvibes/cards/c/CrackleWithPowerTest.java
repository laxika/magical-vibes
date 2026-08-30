package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrackleWithPowerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals five times X damage to each target")
    void dealsFiveTimesXDamageToEachTarget() {
        harness.setLife(player1, 30);
        harness.setLife(player2, 30);
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castSorcery(player1, 0, 2, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("May choose fewer than X targets")
    void mayChooseFewerThanXTargets() {
        harness.setLife(player2, 30);
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 11);

        harness.castSorcery(player1, 0, 3, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Rejects more than X targets")
    void rejectsMoreThanXTargets() {
        harness.setHand(player1, List.of(new CrackleWithPower()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1,
                List.of(player1.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
