package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoyagerDrake.class, GrizzlyBears.class})
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

    @Test
    @DisplayName("A kicked Drake can choose no targets even when creatures are available")
    void canChooseNoTargetsWhenKicked() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castVoyagerDrake(List.of("{U}"));

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(bear.hasKeyword(Keyword.FLYING)).isFalse();
        harness.assertOnBattlefield(player1, "Voyager Drake");
    }

    @Test
    @DisplayName("Two kicks can grant flying to only one creature controlled by the caster")
    void canChooseFewerTargetsThanPayments() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castVoyagerDrake(List.of("{U}", "{U}"));

        harness.handlePermanentChosen(player1, chosen.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(chosen.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(unchosen.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Granted flying ends at cleanup")
    void grantedFlyingEndsAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castVoyagerDrake(List.of("{U}"));
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        assertThat(bear.hasKeyword(Keyword.FLYING)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(bear.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Multikicker permits more than one hundred targets when paid that many times")
    void targetCountIsNotCappedAtOneHundred() {
        List<Permanent> targets = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            targets.add(harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()));
        }
        castVoyagerDrake(Collections.nCopies(101, "{U}"));

        for (Permanent target : targets) {
            harness.handlePermanentChosen(player1, target.getId());
        }
        harness.passBothPriorities();

        assertThat(targets).allSatisfy(target ->
                assertThat(target.hasKeyword(Keyword.FLYING)).isTrue());
    }

    private void castVoyagerDrake(List<String> payments) {
        harness.setHand(player1, List.of(new VoyagerDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1 + payments.size());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithRepeatedCosts(player1, 0, payments);
        harness.passBothPriorities();
    }
}
