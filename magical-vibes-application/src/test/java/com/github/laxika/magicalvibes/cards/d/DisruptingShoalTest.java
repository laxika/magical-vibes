package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisruptingShoalTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell when its mana value equals X")
    void countersSpellWithMatchingManaValue() {
        GrizzlyBears bears = new GrizzlyBears(); // MV 2
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new DisruptingShoal()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The spell resolves when its mana value differs from X")
    void doesNotCounterOnManaValueMismatch() {
        Shock shock = new Shock(); // MV 1
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new DisruptingShoal()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 3, shock.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Disrupting Shoal");
        harness.assertLife(player2, lifeBefore - 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiling a blue card with mana value X pays the alternative cost")
    void alternativeCostExilesBlueCardWithManaValueX() {
        GrizzlyBears bears = new GrizzlyBears(); // MV 2
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        // Disrupting Shoal's own mana value is 2, so exiling it pays for X = 2 with no mana spent.
        harness.setHand(player2, List.of(new DisruptingShoal(), new DisruptingShoal()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithAlternateExileFromHand(player2, 0, 2, bears.getId(), 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The exiled card's mana value must equal the chosen X")
    void alternativeCostRejectsMismatchedManaValue() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new DisruptingShoal(), new DisruptingShoal()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() ->
                harness.castInstantWithAlternateExileFromHand(player2, 0, 3, bears.getId(), 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).hasSize(1);
    }
}
