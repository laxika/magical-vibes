package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SimicGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BaldursGate.class, SimicGuildgate.class})
class BaldursGateTest extends BaseCardTest {

    @Test
    void firstAbilityAddsColorlessMana() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new BaldursGate());

        harness.activateAbility(player1, indexOf(gate), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void secondAbilityAddsManaForOtherGatesYouControl() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new BaldursGate());
        harness.addToBattlefield(player1, new SimicGuildgate());
        harness.addToBattlefield(player1, new SimicGuildgate());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(gate), 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void secondAbilityExcludesThisGateAndOpponentsGates() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new BaldursGate());
        harness.addToBattlefield(player1, new SimicGuildgate());
        harness.addToBattlefield(player2, new SimicGuildgate());
        harness.addToBattlefield(player2, new SimicGuildgate());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(gate), 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void secondAbilityWithNoOtherGatesPaysCostsAndAddsNoMana() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new BaldursGate());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(gate), 1, null, null);

        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void secondAbilityProducesEntireBatchInTheChosenColor(ManaColor color) {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new BaldursGate());
        harness.addToBattlefield(player1, new SimicGuildgate());
        harness.addToBattlefield(player1, new SimicGuildgate());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(gate), 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void secondAbilityCannotUseItsOwnProceedsToPayItsCost() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new BaldursGate());
        harness.addToBattlefield(player1, new SimicGuildgate());
        harness.addToBattlefield(player1, new SimicGuildgate());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(gate), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gate.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void firstAbilityTapsTheGateAndResolvesWithoutTheStack() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new BaldursGate());

        harness.activateAbility(player1, indexOf(gate), 0, null, null);

        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(gate), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
