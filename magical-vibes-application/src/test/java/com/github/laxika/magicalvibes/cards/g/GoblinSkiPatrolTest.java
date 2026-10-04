package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Goblin Ski Patrol")
@CardUsed({GoblinSkiPatrol.class, Mountain.class, RayOfCommand.class, SnowCoveredForest.class, SnowCoveredMountain.class})
class GoblinSkiPatrolTest extends BaseCardTest {

    private Permanent addPatrol() {
        return addCreatureReady(player1, new GoblinSkiPatrol());
    }

    private void addSnowMountain() {
        harness.addToBattlefield(player1, new SnowCoveredMountain());
    }

    private void payMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Activating gives +2/+0 and flying")
    void activationBoostsAndGrantsFlying() {
        Permanent patrol = addPatrol();
        addSnowMountain();
        payMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, patrol)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, patrol)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("It is sacrificed at the beginning of the next end step")
    void sacrificedAtNextEndStep() {
        addPatrol();
        addSnowMountain();
        payMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Goblin Ski Patrol");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Ski Patrol");
        harness.assertInGraveyard(player1, "Goblin Ski Patrol");
    }

    @Test
    @DisplayName("Cannot activate without a snow Mountain")
    void cannotActivateWithoutSnowMountain() {
        addPatrol();
        harness.addToBattlefield(player1, new Mountain());
        payMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with a snow Forest instead of a snow Mountain")
    void cannotActivateWithSnowForest() {
        addPatrol();
        harness.addToBattlefield(player1, new SnowCoveredForest());
        payMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate a second time")
    void cannotActivateTwice() {
        addPatrol();
        addSnowMountain();
        payMana();
        payMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Still cannot activate again on a later turn")
    void cannotActivateAgainNextTurn() {
        addPatrol();
        addSnowMountain();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            payMana();
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
        });

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.assertOnBattlefield(player1, "Goblin Ski Patrol");
        payMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("The controller at the next end step sacrifices it after a control change")
    void currentControllerSacrificesItAtNextEndStep() {
        Permanent patrol = addPatrol();
        addSnowMountain();

        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            payMana();
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            harness.setHand(player2, List.of(new RayOfCommand()));
            harness.addMana(player2, ManaColor.BLUE, 4);
            harness.castAndResolveInstant(player2, 0, patrol.getId());
            harness.assertOnBattlefield(player2, "Goblin Ski Patrol");
        });

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Ski Patrol");
        harness.assertNotOnBattlefield(player2, "Goblin Ski Patrol");
        harness.assertInGraveyard(player1, "Goblin Ski Patrol");
    }

    @Test
    @DisplayName("Boost and flying persist until the next end step when activated during an end step")
    void activationDuringEndStepKeepsEffectsUntilNextEndStep() {
        Permanent patrol = addPatrol();
        addSnowMountain();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            payMana();
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
        });

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.assertOnBattlefield(player1, "Goblin Ski Patrol");

        assertThat(gqs.getEffectivePower(gd, patrol)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, patrol)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.FLYING)).isTrue();

    }

    @Test
    @DisplayName("Activation during an end step schedules sacrifice for the opponent's next end step")
    void activationDuringEndStepSacrificesAtOpponentsEndStep() {
        addPatrol();
        addSnowMountain();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            payMana();
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
            harness.assertOnBattlefield(player1, "Goblin Ski Patrol");
        });
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Ski Patrol");
        harness.assertInGraveyard(player1, "Goblin Ski Patrol");
    }

    @Test
    @DisplayName("An opponent's snow Mountain does not satisfy the activation restriction")
    void cannotActivateWithOpponentsSnowMountain() {
        addPatrol();
        harness.addToBattlefield(player2, new SnowCoveredMountain());
        payMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate again while the first activation is still on the stack")
    void cannotActivateTwiceBeforeResolution() {
        addPatrol();
        addSnowMountain();
        payMana();
        payMana();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Goblin Ski Patrol");
    }

    @Test
    @DisplayName("The ability can be activated while the creature has summoning sickness")
    void canActivateWithSummoningSickness() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new GoblinSkiPatrol());
        addSnowMountain();
        payMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, patrol)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.FLYING)).isTrue();
    }
}
