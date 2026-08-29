package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FoilTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell when cast by discarding an Island and another card")
    void countersTargetSpellWithAlternateCost() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Foil(), new Island(), new Shock()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithAlternateDiscards(player2, 0, bears.getId(), 1, List.of(2));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Foil");
        harness.assertInGraveyard(player2, "Island");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The alternate cost requires an Island and another distinct card")
    void alternateCostRequiresTwoMatchingCards() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Foil(), new Island()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateDiscards(
                player2, 0, bears.getId(), 1, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The first alternate discard must be an Island card")
    void alternateCostRequiresIsland() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Foil(), new Shock(), new Shock()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateDiscards(
                player2, 0, bears.getId(), 1, List.of(2)))
                .isInstanceOf(IllegalStateException.class);
    }
}
