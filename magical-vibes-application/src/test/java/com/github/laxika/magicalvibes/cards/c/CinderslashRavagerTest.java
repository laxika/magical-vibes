package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CinderslashRavagerTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each oil-counter permanent you control")
    void costsLessForEachOilCounterPermanentYouControl() {
        addOilPermanent(player1);
        addOilPermanent(player1);
        harness.setHand(player1, List.of(new CinderslashRavager()));
        addManaForReducedCost();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cinderslash Ravager");
    }

    @Test
    @DisplayName("Oil counters on opponents' permanents do not reduce its cost")
    void opponentOilCountersDoNotReduceCost() {
        addOilPermanent(player2);
        harness.setHand(player1, List.of(new CinderslashRavager()));
        addManaForReducedCost();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB deals 1 damage only to creatures opponents control")
    void etbDamagesOnlyOpponentsCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CinderslashRavager()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addOilPermanent(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setCounterCount(CounterType.OIL, 1);
        return permanent;
    }

    private void addManaForReducedCost() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
