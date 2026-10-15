package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(StoneHavenMedic.class)
class StoneHavenMedicTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {W} and tapping Stone Haven Medic gains 1 life")
    void payingWhiteManaAndTappingGainsLife() {
        Permanent medic = addCreatureReady(player1, new StoneHavenMedic());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(medic.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void gainsLifeOnlyOnResolutionAndOnlyForAbilityController() {
        Permanent medic = addCreatureReady(player2, new StoneHavenMedic());
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, null);

        harness.assertLife(player2, 10);
        assertThat(medic.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        harness.assertLife(player2, 11);
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent medic = harness.addToBattlefieldAndReturn(player1, new StoneHavenMedic());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(medic.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent medic = addCreatureReady(player1, new StoneHavenMedic());
        medic.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void colorlessManaCannotPayWhiteActivationCost() {
        Permanent medic = addCreatureReady(player1, new StoneHavenMedic());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(medic.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
