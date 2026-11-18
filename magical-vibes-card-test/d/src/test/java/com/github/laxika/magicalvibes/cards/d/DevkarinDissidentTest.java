package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevkarinDissident.class})
class DevkarinDissidentTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability gives +2/+2 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent dissident = addReadyDissident();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, dissident)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, dissident)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability can be used repeatedly")
    void activatedAbilityBoostsSelfRepeatedly() {
        Permanent dissident = addReadyDissident();
        addAbilityMana();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, dissident)).isEqualTo(4);
        assertThat(PermanentGrants.toughnessBoostOf(gd, dissident)).isEqualTo(4);
    }

    @Test
    @DisplayName("Activated ability boost wears off at end of turn")
    void activatedAbilityBoostResetsAtEndOfTurn() {
        Permanent dissident = addReadyDissident();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, dissident)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, dissident)).isZero();
    }

    @Test
    @DisplayName("Tapped, summoning-sick Dissident can activate without tapping")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent dissident = harness.addToBattlefieldAndReturn(player1, new DevkarinDissident());
        dissident.setSummoningSick(true);
        dissident.tap();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(PermanentGrants.powerBoostOf(gd, dissident)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, dissident)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, dissident)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, dissident)).isEqualTo(2);
        assertThat(dissident.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the activating Dissident gets the boost")
    void boostsOnlyItsSource() {
        Permanent other = addReadyDissident();
        Permanent source = addReadyDissident();
        Permanent opponent = addCreatureReady(player2, new DevkarinDissident());
        addAbilityMana();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, source)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, source)).isEqualTo(2);
        assertThat(source.isTapped()).isFalse();
        assertThat(PermanentGrants.powerBoostOf(gd, other)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, other)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opponent)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponent)).isZero();
    }

    @Test
    @DisplayName("Activation requires green mana")
    void cannotActivateWithoutGreenMana() {
        Permanent dissident = addReadyDissident();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, dissident)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, dissident)).isZero();
    }

    @Test
    @DisplayName("Activation requires all five mana")
    void cannotActivateWithTooLittleMana() {
        Permanent dissident = addReadyDissident();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, dissident)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, dissident)).isZero();
    }
    private Permanent addReadyDissident() {
        return addCreatureReady(player1, new DevkarinDissident());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
