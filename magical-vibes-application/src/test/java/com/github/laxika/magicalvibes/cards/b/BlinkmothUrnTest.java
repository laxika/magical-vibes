package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.PowerstoneShard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlinkmothUrnTest extends BaseCardTest {

    @Test
    @DisplayName("Adds colorless mana for each artifact its controller controls")
    void addsManaForControlledArtifacts() {
        harness.addToBattlefield(player1, new BlinkmothUrn());
        harness.addToBattlefield(player1, new PowerstoneShard());
        harness.addToBattlefield(player1, new PowerstoneShard());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Adds the mana to the player whose first main phase it is")
    void addsManaToActivePlayer() {
        harness.addToBattlefield(player1, new BlinkmothUrn());
        harness.addToBattlefield(player1, new PowerstoneShard());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Does not trigger while the urn is tapped")
    void doesNotTriggerWhileTapped() {
        Permanent urn = harness.addToBattlefieldAndReturn(player1, new BlinkmothUrn());
        harness.addToBattlefield(player1, new PowerstoneShard());
        urn.tap();

        advanceToPrecombatMain(player1);
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
