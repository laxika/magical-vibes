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
        harness.passBothPriorities();

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
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    void opponentCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new JawsOfDefeat());
        harness.enterBattlefieldAndReturn(player2, new GiantSpider());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void usesChangedPowerAtResolutionWhenPowerExceedsToughness() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new JawsOfDefeat());
        var creature = harness.enterBattlefieldAndReturn(player1, new GiantSpider());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        creature.setPowerModifier(5);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void negativePowerIsIncludedInDifference() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new JawsOfDefeat());
        var creature = harness.enterBattlefieldAndReturn(player1, new GiantSpider());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        creature.setPowerModifier(-5);
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
    }

    @Test
    void usesLastKnownNegativeToughnessAfterCreatureDies() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new JawsOfDefeat());
        var creature = harness.enterBattlefieldAndReturn(player1, new GiantSpider());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        creature.setToughnessModifier(-5);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Giant Spider");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }
}
