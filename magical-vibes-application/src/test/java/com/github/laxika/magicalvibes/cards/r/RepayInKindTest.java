package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RepayInKind.class})
class RepayInKindTest extends BaseCardTest {

    @Test
    @DisplayName("Each player's life total becomes the lowest life total among all players")
    void setsEachPlayerLifeToLowestTotal() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 5);
        harness.setHand(player1, List.of(new RepayInKind()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

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

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(30);
        assertThat(gd.getLife(player2.getId())).isEqualTo(30);
    }

    @Test
    @DisplayName("Equal life totals remain unchanged")
    void equalLifeTotalsRemainUnchanged() {
        harness.setLife(player1, 8);
        harness.setLife(player2, 8);
        harness.setHand(player1, List.of(new RepayInKind()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 8);
        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("The lowest life total is determined at resolution rather than casting")
    void usesLifeTotalsAtResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 5);
        harness.setHand(player1, List.of(new RepayInKind()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 0);
        harness.setLife(player1, 3);
        harness.setLife(player2, 10);
        harness.passBothPriorities();

        harness.assertLife(player1, 3);
        harness.assertLife(player2, 3);
    }
}
