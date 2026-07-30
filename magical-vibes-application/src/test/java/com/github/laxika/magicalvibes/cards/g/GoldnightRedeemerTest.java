package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoldnightRedeemerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each other creature you control")
    void gainsTwoLifePerOtherOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoldnightRedeemer()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Does not count itself")
    void doesNotCountItself() {
        harness.setHand(player1, List.of(new GoldnightRedeemer()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not count creatures the opponent controls")
    void ignoresOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoldnightRedeemer()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }
}
