package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SimicGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
