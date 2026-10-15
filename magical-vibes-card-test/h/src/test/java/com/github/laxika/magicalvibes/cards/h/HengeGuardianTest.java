package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HengeGuardian.class})
class HengeGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability grants trample")
    void abilityGrantsTrample() {
        Permanent guardian = addCreatureReady(player1, new HengeGuardian());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample wears off at end of turn")
    void trampleWearsOff() {
        Permanent guardian = addCreatureReady(player1, new HengeGuardian());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new HengeGuardian());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate without waiting for summoning sickness to end")
    void canActivateWhileSummoningSick() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new HengeGuardian());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A tapped Guardian can activate without untapping")
    void canActivateWhileTapped() {
        Permanent guardian = addCreatureReady(player1, new HengeGuardian());
        guardian.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
        assertThat(guardian.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the source Guardian gains trample, and only after resolution")
    void grantsTrampleOnlyToSourceOnResolution() {
        Permanent otherGuardian = addCreatureReady(player1, new HengeGuardian());
        Permanent guardian = addCreatureReady(player1, new HengeGuardian());
        Permanent opposingGuardian = addCreatureReady(player2, new HengeGuardian());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherGuardian, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingGuardian, Keyword.TRAMPLE)).isFalse();
    }
}
