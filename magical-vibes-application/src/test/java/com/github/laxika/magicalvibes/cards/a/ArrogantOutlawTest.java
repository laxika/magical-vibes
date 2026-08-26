package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ArrogantOutlaw.class)
class ArrogantOutlawTest extends BaseCardTest {

    @Test
    @DisplayName("Does nothing when no opponent lost life this turn")
    void noTriggerWithoutOpponentLifeLoss() {
        castOutlaw();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each opponent loses 2 life and you gain 2 life after an opponent lost life")
    void drainsAfterOpponentLifeLoss() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        castOutlaw();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private void castOutlaw() {
        harness.setHand(player1, List.of(new ArrogantOutlaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
