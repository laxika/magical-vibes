package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThwartTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell for its mana cost")
    void countersTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Thwart()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Thwart");
    }

    @Test
    @DisplayName("Counters a spell by returning three Islands to hand")
    void countersTargetSpellWithAlternateCost() {
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent thirdIsland = harness.addToBattlefieldAndReturn(player2, new Island());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Thwart()));
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithAlternateCost(player2, 0, bears.getId(),
                List.of(firstIsland.getId(), secondIsland.getId(), thirdIsland.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Island", "Island", "Island");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Alternate cost requires three Islands")
    void alternateCostRequiresThreeIslands() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Thwart()));

        List<Permanent> islands = List.of(
                harness.addToBattlefieldAndReturn(player2, new Island()),
                harness.addToBattlefieldAndReturn(player2, new Island()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player2, 0, bears.getId(),
                islands.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
    }
}
