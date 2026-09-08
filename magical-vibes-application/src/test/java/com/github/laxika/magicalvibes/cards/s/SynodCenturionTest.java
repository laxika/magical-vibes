package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SynodCenturionTest extends BaseCardTest {

    @Test
    @DisplayName("Survives while controlling another artifact")
    void survivesWithAnotherArtifact() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        castCenturion();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Synod Centurion");
        harness.assertOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("Sacrifices itself when controlling no other artifacts")
    void sacrificesWhenNoOtherArtifacts() {
        castCenturion();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Synod Centurion");
        harness.assertInGraveyard(player1, "Synod Centurion");
    }

    @Test
    @DisplayName("Sacrifices itself when the last other artifact leaves")
    void sacrificesWhenLastOtherArtifactLeaves() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        castCenturion();

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, fountainId);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Synod Centurion");
        harness.assertInGraveyard(player1, "Synod Centurion");
    }

    @Test
    @DisplayName("An opponent's artifact does not satisfy the condition")
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        castCenturion();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Synod Centurion");
        harness.assertInGraveyard(player1, "Synod Centurion");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    private void castCenturion() {
        harness.setHand(player1, List.of(new SynodCenturion()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }
}
