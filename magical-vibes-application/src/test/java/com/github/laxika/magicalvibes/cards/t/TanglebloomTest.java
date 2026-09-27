package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Tanglebloom.class)
class TanglebloomTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps Tanglebloom and consumes 1 mana")
    void activatingTapsAndConsumesMana() {
        Permanent tanglebloom = harness.addToBattlefieldAndReturn(player1, new Tanglebloom());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThat(tanglebloom.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, null);

        assertThat(tanglebloom.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving ability gains 1 life for the controller only")
    void resolvingGainsOneLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Tanglebloom());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Activating ability puts it on the stack without gaining life immediately")
    void activatingPutsAbilityOnStack() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Tanglebloom());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new Tanglebloom());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate twice in a turn because it requires tap")
    void cannotActivateTwice() {
        harness.addToBattlefield(player1, new Tanglebloom());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
