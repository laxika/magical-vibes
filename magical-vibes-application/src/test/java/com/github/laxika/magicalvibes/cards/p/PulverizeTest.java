package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KyrenToy;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RamosianSergeant;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Pulverize.class, KyrenToy.class, RamosianSergeant.class, Mountain.class, Island.class})
class PulverizeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all artifacts controlled by both players")
    void destroysAllArtifacts() {
        harness.addToBattlefield(player1, new KyrenToy());
        harness.addToBattlefield(player2, new KyrenToy());
        harness.addToBattlefield(player1, new RamosianSergeant());
        harness.castFromHand(player1, new Pulverize(), "{4}{R}{R}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kyren Toy");
        harness.assertNotOnBattlefield(player2, "Kyren Toy");
        harness.assertOnBattlefield(player1, "Ramosian Sergeant");
    }

    @Test
    @DisplayName("Alternate cost sacrifices two Mountains and destroys all artifacts")
    void castBySacrificingTwoMountains() {
        UUID mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        UUID mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.addToBattlefield(player2, new KyrenToy());
        harness.setHand(player1, List.of(new Pulverize()));

        harness.castWithAlternateCost(player1, 0, List.of(mountain1, mountain2));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Kyren Toy");
    }

    @Test
    @DisplayName("Alternate cost fails when it does not sacrifice two Mountains")
    void alternateCostRequiresTwoMountains() {
        UUID mountain = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.setHand(player1, List.of(new Pulverize()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(mountain)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cost fails when sacrificing a non-Mountain")
    void alternateCostFailsWithNonMountain() {
        UUID mountain = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        UUID island = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        harness.setHand(player1, List.of(new Pulverize()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(mountain, island)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cost requires two distinct Mountains")
    void alternateCostRejectsDuplicateMountain() {
        UUID mountain = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.setHand(player1, List.of(new Pulverize()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(mountain, mountain)))
                .isInstanceOf(IllegalStateException.class);
    }
}
