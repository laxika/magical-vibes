package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VoyagerDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Without multikicker, the ETB grants flying to no creatures")
    void withoutMultikickerGrantsToNoCreatures() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castVoyagerDrake(List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(bear.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("One multikicker payment grants flying to up to one target creature")
    void oneMultikickerPaymentGrantsFlyingToOneTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castVoyagerDrake(List.of("{U}"));

        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Two multikicker payments grant flying to up to two target creatures")
    void twoMultikickerPaymentsGrantFlyingToTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castVoyagerDrake(List.of("{U}", "{U}"));

        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(second.hasKeyword(Keyword.FLYING)).isTrue();
    }

    private void castVoyagerDrake(List<String> payments) {
        harness.setHand(player1, List.of(new VoyagerDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1 + payments.size());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                payments, false);
        harness.passBothPriorities();
    }
}
