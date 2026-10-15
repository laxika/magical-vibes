package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.x.X23DeadlyWeapon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WolverineClawsOut.class, GrizzlyBears.class, ForestBear.class,
        X23DeadlyWeapon.class, Disfigure.class})
class WolverineClawsOutTest extends BaseCardTest {

    @Test
    @DisplayName("A Mutant you control attacking doubles its power until end of turn")
    void mutantAttackerDoublesPower() {
        Permanent wolverine = addCreatureReady(player1, new WolverineClawsOut());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolverine)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wolverine)).isEqualTo(5);
    }

    @Test
    @DisplayName("A non-Mutant attacker does not trigger the power doubling")
    void nonMutantAttackerDoesNotTrigger() {
        addCreatureReady(player1, new WolverineClawsOut());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Wolverine may assign combat damage as though it were unblocked")
    void blockedWolverineAssignsDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent wolverine = addCreatureReady(player1, new WolverineClawsOut());
        Permanent blocker = addCreatureReady(player2, new ForestBear());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(wolverine))));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 4));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertOnBattlefield(player2, "Forest Bear");
    }

    @Test
    @DisplayName("The power doubling wears off at end of turn")
    void powerDoublingWearsOffAtEndOfTurn() {
        Permanent wolverine = addCreatureReady(player1, new WolverineClawsOut());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, wolverine)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolverine)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each attacking Mutant gets its own power doubling, including another Mutant")
    void eachMutantAttackerDoublesItsOwnPower() {
        Permanent wolverine = addCreatureReady(player1, new WolverineClawsOut());
        Permanent x23 = addCreatureReady(player1, new X23DeadlyWeapon());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, wolverine)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, x23)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, x23)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's attacking Mutant does not trigger Wolverine")
    void opposingMutantDoesNotTrigger() {
        addCreatureReady(player1, new WolverineClawsOut());
        Permanent x23 = addCreatureReady(player2, new X23DeadlyWeapon());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, x23)).isEqualTo(3);
    }

    @Test
    @DisplayName("Doubling negative power makes that power twice as negative")
    void negativePowerIsDoubled() {
        Permanent wolverine = addCreatureReady(player1, new WolverineClawsOut());
        harness.setHand(player1, List.of(new Disfigure(), new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castAndResolveInstant(player1, 0, wolverine.getId());
            harness.castAndResolveInstant(player1, 0, wolverine.getId());
        });
        assertThat(gqs.getEffectivePower(gd, wolverine)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, wolverine)).isEqualTo(1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, wolverine)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, wolverine)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power is read when the attack trigger resolves, including zero power")
    void doublingUsesPowerAtResolution() {
        Permanent wolverine = addCreatureReady(player1, new WolverineClawsOut());
        harness.setHand(player1, List.of(new Disfigure()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.castAndResolveInstant(player1, 0, wolverine.getId());
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, wolverine)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wolverine)).isEqualTo(3);
    }

    @Test
    @DisplayName("Wolverine can choose normal blocked damage assignment")
    void blockedWolverineCanDamageBlockerInstead() {
        harness.setLife(player2, 20);
        Permanent wolverine = addCreatureReady(player1, new WolverineClawsOut());
        Permanent blocker = addCreatureReady(player2, new X23DeadlyWeapon());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "X-23, Deadly Weapon");
        assertThat(wolverine.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Without trample Wolverine cannot split damage between blocker and player")
    void cannotMixBlockedAndUnblockedDamageAssignments() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WolverineClawsOut());
        Permanent blocker = addCreatureReady(player2, new X23DeadlyWeapon());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 3, player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 4));

        harness.assertLife(player2, 16);
        harness.assertOnBattlefield(player2, "X-23, Deadly Weapon");
    }
}
