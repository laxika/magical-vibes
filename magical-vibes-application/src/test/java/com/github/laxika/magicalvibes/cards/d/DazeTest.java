package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DazeTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when cast for its mana cost")
    void countersSpellForManaCost() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Daze()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Daze");
    }

    @Test
    @DisplayName("Can be cast by returning an Island to its owner's hand")
    void countersSpellByReturningIsland() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player2, List.of(new Daze()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithAlternateCost(player2, 0, bears.getId(), List.of(island.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Daze");
        harness.assertInHand(player2, "Island");
    }

    @Test
    @DisplayName("Alternate cost rejects a non-Island")
    void alternateCostRejectsNonIsland() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player2, List.of(new Daze()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player2, 0, bears.getId(), List.of(mountain.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
