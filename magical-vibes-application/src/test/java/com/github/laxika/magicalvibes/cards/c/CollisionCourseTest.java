package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HoneymoonHearse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollisionCourseTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode counts creatures and Vehicles the controller controls")
    void damageModeCountsCreaturesAndVehicles() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HoneymoonHearse());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollisionCourse()));
        addMana();

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damage mode does not count noncreature non-Vehicle artifacts")
    void damageModeDoesNotCountOtherArtifacts() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollisionCourse()));
        addMana();

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroy mode destroys a target artifact")
    void destroyModeDestroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new CollisionCourse()));
        addMana();

        harness.castSorcery(player1, 0, 1, harness.getPermanentId(player2, "Fountain of Youth"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Each mode enforces its target restriction")
    void modesRejectIllegalTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new CollisionCourse()));
        addMana();

        var creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        var artifactId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, artifactId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
