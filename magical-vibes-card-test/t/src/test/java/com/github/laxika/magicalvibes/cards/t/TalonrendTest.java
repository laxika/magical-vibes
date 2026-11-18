package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Talonrend.class)
class TalonrendTest extends BaseCardTest {

    @Test
    @DisplayName("{U/R} paid with blue: gets +1/-1 until end of turn")
    void pumpPaidWithBlue() {
        Permanent talonrend = addCreatureReady(player1, new Talonrend());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, talonrend)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, talonrend)).isEqualTo(-1);
    }

    @Test
    @DisplayName("{U/R} paid with red: hybrid cost accepts either color")
    void pumpPaidWithRed() {
        Permanent talonrend = addCreatureReady(player1, new Talonrend());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, talonrend)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, talonrend)).isEqualTo(-1);
    }

    @Test
    @DisplayName("Ability stacks when activated repeatedly")
    void pumpStacks() {
        Permanent talonrend = addCreatureReady(player1, new Talonrend());
        harness.addMana(player1, ManaColor.BLUE, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(PermanentGrants.powerBoostOf(gd, talonrend)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, talonrend)).isEqualTo(-3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent talonrend = addCreatureReady(player1, new Talonrend());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, talonrend)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, talonrend)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, talonrend)).isEqualTo(0);
    }

    @Test
    @DisplayName("The fifth activation puts Talonrend into the graveyard for zero toughness")
    void diesWhenPumpReducesToughnessToZero() {
        addCreatureReady(player1, new Talonrend());
        harness.addMana(player1, ManaColor.BLUE, 5);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
        harness.assertOnBattlefield(player1, "Talonrend");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Talonrend");
        harness.assertInGraveyard(player1, "Talonrend");
    }

    @Test
    @DisplayName("Only the Talonrend whose ability was activated gets the boost")
    void boostsOnlyItsSource() {
        Permanent source = addCreatureReady(player1, new Talonrend());
        Permanent other = addCreatureReady(player1, new Talonrend());
        Permanent opposing = addCreatureReady(player2, new Talonrend());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, source)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, source)).isEqualTo(-1);
        assertThat(PermanentGrants.powerBoostOf(gd, other)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, other)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opposing)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opposing)).isZero();
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent talonrend = addCreatureReady(player1, new Talonrend());
        talonrend.setSummoningSick(true);
        talonrend.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, talonrend)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, talonrend)).isEqualTo(-1);
        assertThat(talonrend.isTapped()).isTrue();
    }
}
