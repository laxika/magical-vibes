package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RepayInKindTest extends BaseCardTest {

    @Test
    @DisplayName("Each player's life total becomes the lowest life total among all players")
    void setsEachPlayerLifeToLowestTotal() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 5);
        harness.setHand(player1, List.of(new RepayInKind()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(5);
        assertThat(gd.getLife(player2.getId())).isEqualTo(5);
    }

    @Test
    @DisplayName("Repay in Kind uses the lowest total when both players are above starting life")
    void setsLifeToLowestTotalAboveStartingLife() {
        harness.setLife(player1, 30);
        harness.setLife(player2, 40);
        harness.setHand(player1, List.of(new RepayInKind()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(30);
        assertThat(gd.getLife(player2.getId())).isEqualTo(30);
    }
}
