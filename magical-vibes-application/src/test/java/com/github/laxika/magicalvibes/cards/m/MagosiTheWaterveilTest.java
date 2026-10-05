package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagosiTheWaterveil.class})
class MagosiTheWaterveilTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and taps for blue mana")
    void entersTappedAndTapsForBlue() {
        harness.setHand(player1, List.of(new MagosiTheWaterveil()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        Permanent magosi = findPermanent(player1, "Magosi, the Waterveil");

        assertThat(magosi.isTapped()).isTrue();

        magosi.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts an eon counter on itself and skips the controller's next turn")
    void putsEonCounterAndSkipsNextTurn() {
        Permanent magosi = addReadyMagosi();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(magosi.getCounterCount(CounterType.EON)).isEqualTo(1);
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes an eon counter, returns itself to hand, and queues an extra turn")
    void removesCounterReturnsToHandAndQueuesExtraTurn() {
        Permanent magosi = addReadyMagosi();
        magosi.setCounterCount(CounterType.EON, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(magosi);
        assertThat(gd.playerHands.get(player1.getId())).contains(magosi.getCard());
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("Cannot use the extra-turn ability without an eon counter")
    void extraTurnAbilityNeedsEonCounter() {
        addReadyMagosi();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The eon-counter ability requires blue mana")
    void counterAbilityRequiresBlueMana() {
        Permanent magosi = addReadyMagosi();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(magosi.isTapped()).isFalse();
        assertThat(magosi.getCounterCount(CounterType.EON)).isZero();
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("The extra-turn ability cannot be activated while Magosi is tapped")
    void extraTurnAbilityRequiresUntappedSource() {
        Permanent magosi = addReadyMagosi();
        magosi.setCounterCount(CounterType.EON, 1);
        magosi.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(magosi.getCounterCount(CounterType.EON)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(magosi);
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Returning Magosi and removing its eon counter are activation costs")
    void paysReturnAndCounterCostsBeforeResolution() {
        Permanent magosi = addReadyMagosi();
        magosi.setCounterCount(CounterType.EON, 1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(magosi.getCounterCount(CounterType.EON)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(magosi);
        assertThat(gd.playerHands.get(player1.getId())).contains(magosi.getCard());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.extraTurns).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("The eon counter and skipped turn happen on resolution, not activation")
    void counterAndSkipWaitForResolution() {
        Permanent magosi = addReadyMagosi();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(magosi.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(magosi.getCounterCount(CounterType.EON)).isZero();
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isZero();

        harness.passBothPriorities();

        assertThat(magosi.getCounterCount(CounterType.EON)).isEqualTo(1);
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller still skips a turn when Magosi leaves before the counter ability resolves")
    void skipsTurnEvenWhenSourceLeavesBeforeResolution() {
        Permanent magosi = addReadyMagosi();
        magosi.setCounterCount(CounterType.EON, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        magosi.untap();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(magosi);
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("A pending skipped turn consumes Magosi's extra turn")
    void skipsTheExtraTurnBeforeTheNextNormalTurn() {
        harness.forceActivePlayer(player1);
        Permanent magosi = addReadyMagosi();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        magosi.untap();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.currentTurnIsExtraTurn).isFalse();
    }

    private Permanent addReadyMagosi() {
        Permanent magosi = harness.addToBattlefieldAndReturn(player1, new MagosiTheWaterveil());
        magosi.untap();
        return magosi;
    }
}
