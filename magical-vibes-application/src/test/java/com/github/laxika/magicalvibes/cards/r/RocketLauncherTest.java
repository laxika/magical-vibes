package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StealArtifact;
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

@CardUsed({RocketLauncher.class, GrizzlyBears.class, StealArtifact.class})
class RocketLauncherTest extends BaseCardTest {

    private void advanceToEndStep() {
        harness.passUntil(player1, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Deals 1 damage to any target and destroys itself at the next end step")
    void damagesPlayerAndDestroysItselfAtEndStep() {
        Permanent launcher = harness.addToBattlefieldAndReturn(player1, new RocketLauncher());
        launcher.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(launcher);

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(launcher);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(launcher);
        harness.assertInGraveyard(player1, "Rocket Launcher");
    }

    @Test
    @DisplayName("Cannot activate before controlling it continuously since the most recent turn began")
    void cannotActivateWhenItIsSummoningSick() {
        harness.addToBattlefield(player1, new RocketLauncher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("continuously");
    }

    @Test
    @DisplayName("Survives the end step without activation")
    void survivesEndStepWithoutActivation() {
        Permanent launcher = harness.addToBattlefieldAndReturn(player1, new RocketLauncher());

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(launcher);
    }

    @Test
    void canActivateRepeatedlyWithoutTapping() {
        Permanent launcher = harness.addToBattlefieldAndReturn(player1, new RocketLauncher());
        launcher.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(launcher.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(launcher);
    }

    @Test
    void dealsOneDamageToCreature() {
        Permanent launcher = harness.addToBattlefieldAndReturn(player1, new RocketLauncher());
        launcher.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void remainingActivationFizzlesAfterCreatureDies() {
        Permanent launcher = harness.addToBattlefieldAndReturn(player1, new RocketLauncher());
        launcher.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();

        advanceToEndStep();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Rocket Launcher");
    }

    @Test
    void activationDuringEndStepWaitsForFollowingEndStep() {
        Permanent launcher = harness.addToBattlefieldAndReturn(player1, new RocketLauncher());
        launcher.setSummoningSick(false);
        advanceToEndStep();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(launcher);

        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(launcher);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Rocket Launcher");
    }

    @Test
    void becomesUsableWhenControllersNextTurnBegins() {
        harness.addToBattlefield(player1, new RocketLauncher());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("continuously");

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    void delayedDestructionRetainsOriginalAbilityControllerAfterTheft() {
        Permanent launcher = harness.addToBattlefieldAndReturn(player1, new RocketLauncher());
        launcher.setSummoningSick(false);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new StealArtifact()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castEnchantment(player2, 0, launcher.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Rocket Launcher");

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Rocket Launcher");
    }

    @Test
    void canActivateWhileTappedAndTargetItsController() {
        Permanent launcher = harness.addToBattlefieldAndReturn(player1, new RocketLauncher());
        launcher.setSummoningSick(false);
        launcher.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(launcher.isTapped()).isTrue();
    }
}
