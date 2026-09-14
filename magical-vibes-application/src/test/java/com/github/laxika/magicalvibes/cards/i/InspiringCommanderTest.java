package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InspiringCommander.class, GrizzlyBears.class, HillGiant.class})
class InspiringCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life and draws a card when a creature with power 2 or less enters")
    void gainsLifeAndDrawsForSmallCreature() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new InspiringCommander());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger for a creature with power greater than 2")
    void doesNotTriggerForLargeCreature() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new InspiringCommander());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature")
    void doesNotTriggerForOpponentCreature() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new InspiringCommander());
        harness.setHand(player1, List.of());

        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when Inspiring Commander enters")
    void doesNotTriggerForItself() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new InspiringCommander()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
