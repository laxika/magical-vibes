package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PulverizeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all artifacts controlled by both players")
    void destroysAllArtifacts() {
        harness.addToBattlefield(player1, new HowlingMine());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pulverize()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Howling Mine");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Alternate cost sacrifices two Mountains and destroys all artifacts")
    void castBySacrificingTwoMountains() {
        UUID mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        UUID mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.addToBattlefield(player2, new HowlingMine());
        harness.setHand(player1, List.of(new Pulverize()));

        harness.castWithAlternateCost(player1, 0, List.of(mountain1, mountain2));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Howling Mine");
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
}
