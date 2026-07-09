package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OonasProwlerTest extends BaseCardTest {

    // ===== Controller activation =====

    @Test
    @DisplayName("Discarding a card shrinks this creature by -2/-0 until end of turn")
    void controllerActivationShrinksProwler() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new OonasProwler());
        int basePower = gqs.getEffectivePower(gd, prowler);
        int baseToughness = gqs.getEffectiveToughness(gd, prowler);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(basePower - 2);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Cannot activate with no card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.addToBattlefieldAndReturn(player1, new OonasProwler());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Any player may activate =====

    @Test
    @DisplayName("An opponent may activate the ability, discarding from their own hand to shrink the Prowler")
    void opponentCanActivate() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new OonasProwler());
        int basePower = gqs.getEffectivePower(gd, prowler);
        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        // player2 doesn't control the Prowler (it's on player1's battlefield at index 0),
        // but the ability is flagged "any player may activate".
        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        // The activating opponent pays the discard cost from their own hand.
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        // The effect still applies to the Prowler its controller owns.
        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(basePower - 2);
    }

    // ===== Ability puts itself on the stack =====

    @Test
    @DisplayName("Paying the discard cost puts the ability on the stack")
    void payingCostStacksAbility() {
        harness.addToBattlefieldAndReturn(player1, new OonasProwler());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    // ===== Wears off at end of turn =====

    @Test
    @DisplayName("The -2/-0 wears off at end of turn cleanup")
    void boostWearsOffAtEndOfTurn() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new OonasProwler());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(prowler.getPowerModifier()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(prowler.getPowerModifier()).isEqualTo(0);
    }
}
