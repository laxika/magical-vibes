package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GerrardsBattleCry.class, SoltariFootSoldier.class})
class GerrardsBattleCryTest extends BaseCardTest {

    @Test
    @DisplayName("Activation gives creatures you control +1/+1 and stacks across activations")
    void pumpsOwnCreatures() {
        addBattleCry();
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new SoltariFootSoldier());
        harness.addMana(player1, ManaColor.WHITE, 6);

        activateAndResolve();

        assertThat(PermanentGrants.powerBoostOf(gd, soldier)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isEqualTo(1);

        activateAndResolve();

        assertThat(PermanentGrants.powerBoostOf(gd, soldier)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's creatures are unaffected")
    void opponentCreaturesUnaffected() {
        addBattleCry();
        Permanent enemySoldier = harness.addToBattlefieldAndReturn(player2, new SoltariFootSoldier());
        harness.addMana(player1, ManaColor.WHITE, 3);

        activateAndResolve();

        assertThat(PermanentGrants.powerBoostOf(gd, enemySoldier)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, enemySoldier)).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addBattleCry();
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new SoltariFootSoldier());
        harness.addMana(player1, ManaColor.WHITE, 3);

        activateAndResolve();

        assertThat(PermanentGrants.powerBoostOf(gd, soldier)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, soldier)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isZero();
    }

    @Test
    @DisplayName("Only creatures present at resolution receive the boost")
    void onlyCreaturesPresentAtResolutionAreBoosted() {
        Permanent battleCry = addBattleCry();
        Permanent existingSoldier = harness.addToBattlefieldAndReturn(player1, new SoltariFootSoldier());
        harness.addMana(player1, ManaColor.WHITE, 3);

        activateAndResolve();

        Permanent laterSoldier = harness.addToBattlefieldAndReturn(player1, new SoltariFootSoldier());

        assertThat(PermanentGrants.powerBoostOf(gd, existingSoldier)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, existingSoldier)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, laterSoldier)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, laterSoldier)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, battleCry)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, battleCry)).isZero();
    }

    @Test
    @DisplayName("Creatures entering after activation but before resolution receive the boost")
    void creaturesEnteringBeforeResolutionAreBoosted() {
        addBattleCry();
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, null);

        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new SoltariFootSoldier());
        assertThat(PermanentGrants.powerBoostOf(gd, soldier)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isZero();

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, soldier)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can resolve with no creatures and does not boost later arrivals")
    void canActivateWithoutCreatures() {
        Permanent battleCry = addBattleCry();
        harness.addMana(player1, ManaColor.WHITE, 3);

        activateAndResolve();

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, battleCry)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, battleCry)).isZero();
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new SoltariFootSoldier());
        assertThat(PermanentGrants.powerBoostOf(gd, soldier)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isZero();
    }

    @Test
    @DisplayName("A tapped Battle Cry can activate and boost tapped creatures")
    void tappedPermanentsDoNotPreventActivationOrBoost() {
        Permanent battleCry = addBattleCry();
        battleCry.tap();
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new SoltariFootSoldier());
        soldier.tap();
        harness.addMana(player1, ManaColor.WHITE, 3);

        activateAndResolve();

        assertThat(PermanentGrants.powerBoostOf(gd, soldier)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, soldier)).isEqualTo(1);
        assertThat(soldier.isTapped()).isTrue();
        assertThat(battleCry.isTapped()).isTrue();
    }

    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addBattleCry() {
        return harness.addToBattlefieldAndReturn(player1, new GerrardsBattleCry());
    }
}
