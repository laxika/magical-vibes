package com.github.laxika.magicalvibes.cards.e;

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

class EnsnareTest extends BaseCardTest {

    @Test
    @DisplayName("Taps all creatures when cast for mana")
    void tapsAllCreaturesForManaCost() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Ensnare()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Ensnare");
    }

    @Test
    @DisplayName("Can be cast by returning two Islands")
    void castsByReturningTwoIslands() {
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Ensnare()));

        harness.castWithAlternateCost(player1, 0, List.of(firstIsland.getId(), secondIsland.getId()));
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Island"))
                .hasSize(2);
        harness.assertInGraveyard(player1, "Ensnare");
    }

    @Test
    @DisplayName("Alternate cost requires two Islands")
    void alternateCostRequiresTwoIslands() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new Ensnare()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
