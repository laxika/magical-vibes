package com.github.laxika.magicalvibes.cards.a;

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

@CardUsed(AngelfireCrusader.class)
class AngelfireCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +1/+0 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent crusader = addCreatureReady(player1, new AngelfireCrusader());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, crusader)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, crusader)).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly for a cumulative boost")
    void repeatedActivationsStack() {
        Permanent crusader = addCreatureReady(player1, new AngelfireCrusader());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, crusader)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, crusader)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate the ability without red mana")
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new AngelfireCrusader());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate the ability with only non-red mana")
    void cannotActivateWithOnlyNonRedMana() {
        addCreatureReady(player1, new AngelfireCrusader());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The ability can be activated while Angelfire Crusader has summoning sickness")
    void activationDoesNotRequireHaste() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player1, new AngelfireCrusader());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThat(crusader.isSummoningSick()).isTrue();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, crusader)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, crusader)).isZero();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent crusader = addCreatureReady(player1, new AngelfireCrusader());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, crusader)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, crusader)).isEqualTo(0);
    }

    @Test
    @DisplayName("A tapped Crusader can activate and boosts only itself when the ability resolves")
    void tappedCrusaderBoostsOnlyItselfOnResolution() {
        Permanent crusader = addCreatureReady(player1, new AngelfireCrusader());
        Permanent otherCrusader = addCreatureReady(player1, new AngelfireCrusader());
        Permanent opposingCrusader = addCreatureReady(player2, new AngelfireCrusader());
        crusader.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(PermanentGrants.powerBoostOf(gd, crusader)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, crusader)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, crusader)).isZero();
        assertThat(crusader.isTapped()).isTrue();
        assertThat(PermanentGrants.powerBoostOf(gd, otherCrusader)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, otherCrusader)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opposingCrusader)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opposingCrusader)).isZero();
    }

}
