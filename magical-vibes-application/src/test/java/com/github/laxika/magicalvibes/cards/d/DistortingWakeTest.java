package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.cards.c.ChromaticSphere;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DistortingWake.class, AncientKavu.class, ChromaticSphere.class, Island.class})
class DistortingWakeTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 returns two target nonland permanents to their owners' hands")
    void returnsXNonlandPermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientKavu());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ChromaticSphere());
        harness.setHand(player1, List.of(new DistortingWake()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=2: {2}{U}{U}{U}

        harness.castSorcery(player1, 0, 2, List.of(creature.getId(), artifact.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ancient Kavu");
        harness.assertNotOnBattlefield(player2, "Chromatic Sphere");
        harness.assertInHand(player2, "Ancient Kavu");
        harness.assertInHand(player2, "Chromatic Sphere");
    }

    @Test
    @DisplayName("X=0 returns no permanents")
    void xZeroReturnsNothing() {
        harness.addToBattlefield(player2, new AncientKavu());
        harness.setHand(player1, List.of(new DistortingWake()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ancient Kavu");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new DistortingWake()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("Cannot target more permanents than X")
    void cannotTargetMoreThanX() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientKavu());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ChromaticSphere());
        harness.setHand(player1, List.of(new DistortingWake()));
        harness.addMana(player1, ManaColor.BLUE, 4); // X=1

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, 1, List.of(creature.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target fewer permanents than X")
    void cannotTargetFewerThanX() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientKavu());
        harness.addToBattlefield(player2, new ChromaticSphere());
        harness.setHand(player1, List.of(new DistortingWake()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=2

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Can target a nonland permanent you control")
    void canTargetOwnNonlandPermanent() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new AncientKavu());
        harness.setHand(player1, List.of(new DistortingWake()));
        harness.addMana(player1, ManaColor.BLUE, 4); // X=1

        harness.castSorcery(player1, 0, 1, List.of(ownPermanent.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ancient Kavu");
        harness.assertInHand(player1, "Ancient Kavu");
    }
}
