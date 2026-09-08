package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MagosiTheWaterveilTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and taps for blue mana")
    void entersTappedAndTapsForBlue() {
        harness.setHand(player1, List.of(new MagosiTheWaterveil()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);

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

    private Permanent addMagosi() {
        return harness.addToBattlefieldAndReturn(player1, new MagosiTheWaterveil());
    }

    private Permanent addReadyMagosi() {
        Permanent magosi = addMagosi();
        magosi.untap();
        return magosi;
    }
}
