package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlowstoneHellion.class})
class FlowstoneHellionTest extends BaseCardTest {

    @Test
    @DisplayName("Haste allows attacking on the turn it enters")
    void canAttackOnTurnItEnters() {
        harness.castFromHand(player1, new FlowstoneHellion(), "{4}{R}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("The zero-cost ability can be activated while tapped")
    void canActivateWhileTapped() {
        Permanent hellion = harness.addToBattlefieldAndReturn(player1, new FlowstoneHellion());
        hellion.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hellion.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, hellion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hellion)).isEqualTo(2);
    }

    @Test
    @DisplayName("Queued activations affect only their source and do nothing after it dies")
    void queuedActivationsDoNotBoostAnotherHellionAfterSourceDies() {
        addCreatureReady(player1, new FlowstoneHellion());
        Permanent other = addCreatureReady(player1, new FlowstoneHellion());

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
        }
        assertThat(gd.stack).hasSize(4);

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(other);
        harness.assertInGraveyard(player1, "Flowstone Hellion");
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating the ability gives +1/-1 until end of turn")
    void activatingAbilityBoostsSelf() {
        Permanent hellion = addCreatureReady(player1, new FlowstoneHellion());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hellion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hellion)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly and the boosts stack")
    void repeatedActivationsStack() {
        Permanent hellion = addCreatureReady(player1, new FlowstoneHellion());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hellion)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hellion)).isEqualTo(1);
    }

    @Test
    @DisplayName("Three activations reduce its toughness to zero and it dies")
    void threeActivationsCauseStateBasedDeath() {
        addCreatureReady(player1, new FlowstoneHellion());

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Flowstone Hellion");
        harness.assertInGraveyard(player1, "Flowstone Hellion");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent hellion = addCreatureReady(player1, new FlowstoneHellion());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hellion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hellion)).isEqualTo(3);
    }
}
