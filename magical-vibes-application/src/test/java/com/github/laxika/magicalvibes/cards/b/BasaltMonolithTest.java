package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BasaltMonolith.class)
class BasaltMonolithTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Basalt Monolith produces three colorless mana")
    void tappingProducesThreeColorlessMana() {
        addReadyMonolith(player1, false);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Basalt Monolith does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent monolith = addReadyMonolith(player1, true);

        advanceToNextTurn(player2);

        assertThat(monolith.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying {3} untaps Basalt Monolith")
    void payingThreeUntapsMonolith() {
        Permanent monolith = addReadyMonolith(player1, true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(monolith.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Basalt Monolith can be tapped again after paying to untap it")
    void canBeTappedAgainAfterUntapping() {
        Permanent monolith = addReadyMonolith(player1, false);

        gs.tapPermanent(gd, player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gs.tapPermanent(gd, player1, 0);

        assertThat(monolith.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Basalt Monolith's untap ability requires {3}")
    void cannotUntapWithoutThreeMana() {
        addReadyMonolith(player1, true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Basalt Monolith can produce mana immediately after entering the battlefield")
    void canProduceManaImmediately() {
        Permanent monolith = harness.addToBattlefieldAndReturn(player1, new BasaltMonolith());

        harness.tapPermanent(player1, 0);

        assertThat(monolith.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The untap ability uses the stack and untaps only its source")
    void untapAbilityUsesStackAndOnlyUntapsSource() {
        Permanent monolith = addReadyMonolith(player1, true);
        Permanent otherMonolith = addReadyMonolith(player1, true);
        Permanent opponentMonolith = addReadyMonolith(player2, true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(monolith.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(monolith.isTapped()).isFalse();
        assertThat(otherMonolith.isTapped()).isTrue();
        assertThat(opponentMonolith.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colored mana can pay the generic untap cost")
    void coloredManaCanPayUntapCost() {
        Permanent monolith = addReadyMonolith(player1, true);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(monolith.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The untap ability can be activated while Basalt Monolith is untapped")
    void canActivateUntapAbilityWhileUntapped() {
        Permanent monolith = addReadyMonolith(player1, false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.tapPermanent(player1, 0);

        assertThat(monolith.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(monolith.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    private Permanent addReadyMonolith(Player player, boolean tapped) {
        Permanent monolith = harness.addToBattlefieldAndReturn(player, new BasaltMonolith());
        monolith.setSummoningSick(false);
        if (tapped) {
            monolith.tap();
        }
        return monolith;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UNTAP);
    }
}
