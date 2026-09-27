package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JawsOfDefeat.class, GiantSpider.class, GrizzlyBears.class})
class JawsOfDefeatTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent loses the absolute power/toughness difference")
    void losesDifferenceForEnteringCreature() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new JawsOfDefeat());
        harness.enterBattlefieldAndReturn(player1, new GiantSpider());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A creature with equal power and toughness causes no life loss")
    void equalPowerAndToughnessCausesNoLifeLoss() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new JawsOfDefeat());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }
}
