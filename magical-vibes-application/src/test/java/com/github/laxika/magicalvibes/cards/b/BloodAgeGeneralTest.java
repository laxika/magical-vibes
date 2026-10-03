package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindSpirit;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodAgeGeneral.class, GrizzlyBears.class, WindSpirit.class})
class BloodAgeGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping boosts attacking Spirits but not other creatures")
    void tappingBoostsAttackingSpiritsOnly() {
        Permanent general = addCreatureReady(player1, new BloodAgeGeneral());
        Permanent spirit = addCreatureReady(player1, new WindSpirit());
        Permanent nonSpirit = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttackingSpirit = addCreatureReady(player1, new WindSpirit());
        int spiritPower = gqs.getEffectivePower(gd, spirit);
        int spiritToughness = gqs.getEffectiveToughness(gd, spirit);
        int nonSpiritPower = gqs.getEffectivePower(gd, nonSpirit);
        int nonAttackingSpiritPower = gqs.getEffectivePower(gd, nonAttackingSpirit);

        spirit.setAttacking(true);
        nonSpirit.setAttacking(true);
        harness.activateAbility(player1, indexOf(player1, general), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(spiritPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(spiritToughness);
        assertThat(gqs.getEffectivePower(gd, nonSpirit)).isEqualTo(nonSpiritPower);
        assertThat(gqs.getEffectivePower(gd, nonAttackingSpirit)).isEqualTo(nonAttackingSpiritPower);
    }

    @Test
    @DisplayName("Spirit boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent general = addCreatureReady(player1, new BloodAgeGeneral());
        Permanent spirit = addCreatureReady(player1, new WindSpirit());
        int spiritPower = gqs.getEffectivePower(gd, spirit);

        spirit.setAttacking(true);
        harness.activateAbility(player1, indexOf(player1, general), null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(spiritPower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(spiritPower);
    }

    @Test
    @DisplayName("Boost applies to attacking Spirits controlled by an opponent")
    void boostsOpponentsAttackingSpirits() {
        Permanent general = addCreatureReady(player1, new BloodAgeGeneral());
        Permanent spirit = addCreatureReady(player2, new WindSpirit());
        int spiritPower = gqs.getEffectivePower(gd, spirit);

        spirit.setAttacking(true);
        harness.activateAbility(player1, indexOf(player1, general), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(spiritPower + 1);
    }

    @Test
    @DisplayName("Only Spirits attacking when the ability resolves receive the boost")
    void affectedSpiritsAreDeterminedOnResolution() {
        Permanent general = addCreatureReady(player1, new BloodAgeGeneral());
        Permanent leavingCombat = addCreatureReady(player1, new BloodAgeGeneral());
        Permanent joiningCombat = addCreatureReady(player1, new BloodAgeGeneral());
        int leavingPower = gqs.getEffectivePower(gd, leavingCombat);
        int joiningPower = gqs.getEffectivePower(gd, joiningCombat);
        leavingCombat.setAttacking(true);

        harness.activateAbility(player1, indexOf(player1, general), null, null);
        assertThat(general.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, leavingCombat)).isEqualTo(leavingPower);
        leavingCombat.setAttacking(false);
        joiningCombat.setAttacking(true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, leavingCombat)).isEqualTo(leavingPower);
        assertThat(gqs.getEffectivePower(gd, joiningCombat)).isEqualTo(joiningPower + 1);
        joiningCombat.setAttacking(false);
        leavingCombat.setAttacking(true);
        assertThat(gqs.getEffectivePower(gd, joiningCombat)).isEqualTo(joiningPower + 1);
        assertThat(gqs.getEffectivePower(gd, leavingCombat)).isEqualTo(leavingPower);
    }

    @Test
    @DisplayName("Ability can resolve with no attacking Spirits")
    void canActivateWithNoAttackingSpirits() {
        Permanent general = addCreatureReady(player1, new BloodAgeGeneral());
        int power = gqs.getEffectivePower(gd, general);

        harness.activateAbility(player1, indexOf(player1, general), null, null);
        harness.passBothPriorities();

        assertThat(general.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, general)).isEqualTo(power);
    }

    @Test
    @DisplayName("A tapped General cannot pay the tap cost again")
    void cannotActivateWhileTapped() {
        Permanent general = addCreatureReady(player1, new BloodAgeGeneral());
        general.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, general), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick General cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new BloodAgeGeneral());
        general.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, general), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(general.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
