package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NimReplica.class, FountainOfYouth.class, GrizzlyBears.class, LlanowarElves.class})
class NimReplicaTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Nim Replica gives target creature -1/-1 until end of turn")
    void givesTargetCreatureMinusOneMinusOne() {
        harness.addToBattlefield(player1, new NimReplica());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertNotOnBattlefield(player1, "Nim Replica");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can target a creature its controller controls")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new NimReplica());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The temporary debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new NimReplica());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The debuff kills a 1/1 creature")
    void killsOneOneCreature() {
        harness.addToBattlefield(player1, new NimReplica());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new NimReplica());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Nim Replica can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent replica = harness.addToBattlefieldAndReturn(player1, new NimReplica());
        replica.tap();
        replica.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NimReplica());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Nim Replica");
        harness.assertOnBattlefield(player2, "Nim Replica");
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Nim Replica");
        harness.assertInGraveyard(player2, "Nim Replica");
    }

    @Test
    @DisplayName("Nim Replica can target itself, then its ability has no legal target")
    void canTargetItselfBeforePayingSacrificeCost() {
        Permanent replica = harness.addToBattlefieldAndReturn(player1, new NimReplica());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new NimReplica());
        addActivationMana();

        harness.activateAbility(player1, 0, null, replica.getId());
        harness.assertNotOnBattlefield(player1, "Nim Replica");
        harness.assertInGraveyard(player1, "Nim Replica");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Nim Replica");
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
    }

    @Test
    @DisplayName("Colorless mana cannot replace the black activation cost")
    void cannotActivateWithoutBlackMana() {
        harness.addToBattlefield(player1, new NimReplica());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NimReplica());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertOnBattlefield(player1, "Nim Replica");
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
