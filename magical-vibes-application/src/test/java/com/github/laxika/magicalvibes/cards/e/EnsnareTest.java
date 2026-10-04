package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SpinelessThug;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ensnare.class, SpinelessThug.class, Island.class})
class EnsnareTest extends BaseCardTest {

    @Test
    @DisplayName("Taps all creatures when cast for mana")
    void tapsAllCreaturesForManaCost() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SpinelessThug());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new SpinelessThug());
        Permanent ownIsland = harness.addToBattlefieldAndReturn(player1, new Island());

        harness.castFromHand(player1, new Ensnare(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isTrue();
        assertThat(ownIsland.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Ensnare");
    }

    @Test
    @DisplayName("Can be cast by returning two Islands")
    void castsByReturningTwoIslands() {
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SpinelessThug());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new SpinelessThug());
        harness.setHand(player1, List.of(new Ensnare()));

        harness.castWithAlternateCost(player1, 0, List.of(firstIsland.getId(), secondIsland.getId()));
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(firstIsland.getCard(), secondIsland.getCard());
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

    @Test
    @DisplayName("Alternate cost cannot use an Island controlled by an opponent")
    void alternateCostRequiresIslandsYouControl() {
        Permanent ownIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opposingIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Ensnare()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(
                player1, 0, List.of(ownIsland.getId(), opposingIsland.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cost cannot return the same Island twice")
    void alternateCostCannotReturnSameIslandTwice() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new Ensnare()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(
                player1, 0, List.of(island.getId(), island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapped Islands can pay the alternate cost before resolution")
    void tappedIslandsAreReturnedBeforeResolution() {
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        firstIsland.tap();
        secondIsland.tap();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SpinelessThug());
        harness.setHand(player1, List.of(new Ensnare()));

        harness.castWithAlternateCost(player1, 0, List.of(firstIsland.getId(), secondIsland.getId()));

        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(firstIsland.getCard(), secondIsland.getCard());
        assertThat(creature.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Ensnare");
    }

    @Test
    @DisplayName("Taps creatures present at resolution, including ones that entered after casting")
    void tapsCreaturesThatEnterBeforeResolution() {
        Permanent alreadyTapped = harness.addToBattlefieldAndReturn(player1, new SpinelessThug());
        alreadyTapped.tap();
        harness.castFromHand(player1, new Ensnare(), "{3}{U}");
        Permanent newCreature = harness.addToBattlefieldAndReturn(player2, new SpinelessThug());

        harness.passBothPriorities();

        assertThat(alreadyTapped.isTapped()).isTrue();
        assertThat(newCreature.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Ensnare");
    }

    @Test
    @DisplayName("Alternate cost cannot return a non-Island permanent")
    void alternateCostRejectsNonIsland() {
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SpinelessThug());
        harness.setHand(player1, List.of(new Ensnare()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(
                player1, 0, List.of(firstIsland.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
