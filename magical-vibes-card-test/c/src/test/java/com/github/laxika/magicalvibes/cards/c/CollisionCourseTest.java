package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HoneymoonHearse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CollisionCourse.class, GrizzlyBears.class, HoneymoonHearse.class, FountainOfYouth.class})
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

    @Test
    @DisplayName("Damage mode deals zero damage without creatures or Vehicles controlled by its caster")
    void damageModeWithNoMatchingPermanents() {
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HoneymoonHearse());
        harness.setHand(player1, List.of(new CollisionCourse()));
        addMana();

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damage counts permanents present at resolution rather than casting")
    void damageCountChangesBeforeResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollisionCourse()));
        addMana();

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.addToBattlefield(player1, new HoneymoonHearse());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature Vehicle counts once and tapped creatures still count")
    void animatedVehicleCountsOnce() {
        var hearse = harness.addToBattlefieldAndReturn(player1, new HoneymoonHearse());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new CollisionCourse()));
        addMana();

        harness.castSorcery(player1, 0, 0, hearse.getId());
        harness.passBothPriorities();

        assertThat(hearse.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Honeymoon Hearse");
    }

    @Test
    @DisplayName("Destroy mode can destroy the controller's own unanimated Vehicle")
    void destroyModeCanDestroyOwnVehicle() {
        var hearse = harness.addToBattlefieldAndReturn(player1, new HoneymoonHearse());
        harness.setHand(player1, List.of(new CollisionCourse()));
        addMana();

        harness.castSorcery(player1, 0, 1, hearse.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Honeymoon Hearse");
        harness.assertInGraveyard(player1, "Honeymoon Hearse");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
