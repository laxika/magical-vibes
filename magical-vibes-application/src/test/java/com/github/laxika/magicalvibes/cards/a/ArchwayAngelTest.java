package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BorosGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArchwayAngelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 2 life for each Gate its controller controls")
    void gainsLifePerControlledGate() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
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

    private void castAngel() {
        harness.setHand(player1, List.of(new ArchwayAngel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
