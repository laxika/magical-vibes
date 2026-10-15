package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VerminGorger.class})
class VerminGorgerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and sacrificing another creature drains each opponent")
    void sacrificesAnotherCreatureToDrainEachOpponent() {
        Permanent source = addCreatureReady(player1, new VerminGorger());
        addCreatureReady(player1, new VerminGorger());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(source.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Vermin Gorger");
    }

    @Test
    @DisplayName("Cannot sacrifice Vermin Gorger itself")
    void cannotSacrificeItself() {
        addCreatureReady(player1, new VerminGorger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while Vermin Gorger is tapped")
    void cannotActivateWhileTapped() {
        Permanent source = addCreatureReady(player1, new VerminGorger());
        source.tap();
        harness.addToBattlefield(player1, new VerminGorger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate a summoning-sick Vermin Gorger")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new VerminGorger());
        addCreatureReady(player1, new VerminGorger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent source = addCreatureReady(player1, new VerminGorger());
        addCreatureReady(player2, new VerminGorger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A summoning-sick creature is sacrificed as a cost before life totals change")
    void paysSacrificeCostBeforeResolving() {
        Permanent source = addCreatureReady(player1, new VerminGorger());
        harness.addToBattlefield(player1, new VerminGorger());

        harness.activateAbility(player1, 0, null, null);

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);
        harness.assertInGraveyard(player1, "Vermin Gorger");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
