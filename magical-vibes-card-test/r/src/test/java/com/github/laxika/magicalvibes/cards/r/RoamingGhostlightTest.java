package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrowsingTyrannodon;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.u.Unsubstantiate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoamingGhostlight.class, DrowsingTyrannodon.class, Island.class, Unsubstantiate.class})
class RoamingGhostlightTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns up to one target non-Spirit creature")
    void etbReturnsNonSpiritCreature() {
        harness.addToBattlefield(player2, new DrowsingTyrannodon());
        castGhostlight(harness.getPermanentId(player2, "Drowsing Tyrannodon"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Drowsing Tyrannodon");
        harness.assertInHand(player2, "Drowsing Tyrannodon");
        harness.assertOnBattlefield(player1, "Roaming Ghostlight");
    }

    @Test
    @DisplayName("A Spirit creature is not a legal target")
    void cannotTargetSpiritCreature() {
        harness.addToBattlefield(player2, new RoamingGhostlight());
        UUID spiritId = harness.getPermanentId(player2, "Roaming Ghostlight");

        assertThatThrownBy(() -> castGhostlight(spiritId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a non-Spirit creature");
    }

    @Test
    @DisplayName("Can enter the battlefield without a target")
    void canEnterWithoutTarget() {
        harness.castFromHand(player1, new RoamingGhostlight(), "{3}{U}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Roaming Ghostlight");
    }

    @Test
    @DisplayName("Can choose no target even when a non-Spirit creature is available")
    void canDeclineAvailableTarget() {
        harness.addToBattlefield(player2, new DrowsingTyrannodon());

        harness.castFromHand(player1, new RoamingGhostlight(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Roaming Ghostlight");
        harness.assertOnBattlefield(player2, "Drowsing Tyrannodon");
        harness.assertNotInHand(player2, "Drowsing Tyrannodon");
    }

    @Test
    @DisplayName("Can return a non-Spirit creature controlled by the ability's controller")
    void canReturnOwnCreature() {
        harness.addToBattlefield(player1, new DrowsingTyrannodon());

        castGhostlight(harness.getPermanentId(player1, "Drowsing Tyrannodon"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Drowsing Tyrannodon");
        harness.assertInHand(player1, "Drowsing Tyrannodon");
        harness.assertNotInHand(player2, "Drowsing Tyrannodon");
        harness.assertOnBattlefield(player1, "Roaming Ghostlight");
    }

    @Test
    @DisplayName("A noncreature permanent is not a legal target")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new Island());
        UUID landId = harness.getPermanentId(player2, "Island");

        assertThatThrownBy(() -> castGhostlight(landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a non-Spirit creature");
    }

    @Test
    @DisplayName("The return ability still resolves after Roaming Ghostlight leaves")
    void abilityResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player2, new DrowsingTyrannodon());
        castGhostlight(harness.getPermanentId(player2, "Drowsing Tyrannodon"));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Unsubstantiate()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Roaming Ghostlight"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Roaming Ghostlight");
        harness.assertInHand(player1, "Roaming Ghostlight");
        harness.assertNotOnBattlefield(player2, "Drowsing Tyrannodon");
        harness.assertInHand(player2, "Drowsing Tyrannodon");
    }

    @Test
    @DisplayName("An unavailable target does not cause another creature to be returned")
    void doesNotRetargetWhenTargetLeaves() {
        harness.addToBattlefield(player2, new DrowsingTyrannodon());
        harness.addToBattlefield(player1, new DrowsingTyrannodon());
        UUID targetId = harness.getPermanentId(player2, "Drowsing Tyrannodon");
        castGhostlight(targetId);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Unsubstantiate()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Drowsing Tyrannodon");
        harness.assertNotOnBattlefield(player2, "Drowsing Tyrannodon");
        harness.assertOnBattlefield(player1, "Drowsing Tyrannodon");
        harness.assertNotInHand(player1, "Drowsing Tyrannodon");
        harness.assertOnBattlefield(player1, "Roaming Ghostlight");
    }

    private void castGhostlight(UUID targetId) {
        harness.setHand(player1, List.of(new RoamingGhostlight()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0, targetId);
    }
}
