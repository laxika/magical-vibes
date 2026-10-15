package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.o.OrzhovGuildgate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchwayAngel.class, AzoriusGuildgate.class, OrzhovGuildgate.class})
class ArchwayAngelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 2 life for each Gate its controller controls")
    void gainsLifePerControlledGate() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new OrzhovGuildgate());
        harness.setLife(player1, 10);

        castAngel();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Does not count Gates controlled by an opponent")
    void doesNotCountOpponentsGates() {
        harness.addToBattlefield(player2, new AzoriusGuildgate());
        harness.setLife(player1, 10);

        castAngel();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Gains no life without a controlled Gate")
    void gainsNoLifeWithoutControlledGates() {
        harness.setLife(player1, 10);

        castAngel();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Counts Gates present when the triggered ability resolves")
    void countsGatesAtResolution() {
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new ArchwayAngel(), "{5}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player2, new OrzhovGuildgate());
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A Gate that leaves before resolution is not counted")
    void doesNotCountGateThatLeftBeforeResolution() {
        var gate = harness.addToBattlefieldAndReturn(player1, new AzoriusGuildgate());
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new ArchwayAngel(), "{5}{W}");
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(gate);
        gd.playerGraveyards.get(player1.getId()).add(gate.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("The triggered ability resolves after the Angel leaves the battlefield")
    void gainsLifeAfterAngelLeavesBattlefield() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new ArchwayAngel(), "{5}{W}");
        harness.passBothPriorities();

        var battlefield = gd.playerBattlefields.get(player1.getId());
        var angel = battlefield.stream()
                .filter(permanent -> permanent.getCard() instanceof ArchwayAngel)
                .findFirst().orElseThrow();
        battlefield.remove(angel);
        gd.playerGraveyards.get(player1.getId()).add(angel.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
    }

    private void castAngel() {
        harness.castFromHand(player1, new ArchwayAngel(), "{5}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
