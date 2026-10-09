package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.cards.c.ChromaticSphere;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.Repulse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DistortingWake.class, AncientKavu.class, ChromaticSphere.class, Island.class, Repulse.class})
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

        harness.castAndResolveSorcery(player1, 0, 0);

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

        harness.castAndResolveSorcery(player1, 0, 1, ownPermanent.getId());

        harness.assertNotOnBattlefield(player1, "Ancient Kavu");
        harness.assertInHand(player1, "Ancient Kavu");
    }

    @Test
    @DisplayName("The same permanent cannot be chosen twice for X=2")
    void cannotChooseDuplicateTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientKavu());
        harness.setHand(player1, List.of(new DistortingWake()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, 2, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A remaining legal target is returned when another target leaves before resolution")
    void returnsRemainingLegalTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientKavu());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ChromaticSphere());
        harness.setHand(player1, List.of(new DistortingWake()));
        harness.setHand(player2, List.of(new Repulse()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 2, List.of(creature.getId(), artifact.getId()));
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.assertOnBattlefield(player2, "Chromatic Sphere");
        harness.passBothPriorities();

        harness.assertInHand(player2, "Ancient Kavu");
        harness.assertInHand(player2, "Chromatic Sphere");
        harness.assertNotOnBattlefield(player2, "Chromatic Sphere");
        harness.assertInGraveyard(player1, "Distorting Wake");
    }

    @Test
    @DisplayName("X=101 can return 101 distinct nonland permanents")
    void canReturnMoreThanOneHundredPermanents() {
        harness.setHand(player2, List.of());
        List<UUID> targets = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            targets.add(harness.addToBattlefieldAndReturn(player2, new AncientKavu()).getId());
        }
        harness.setHand(player1, List.of(new DistortingWake()));
        harness.addMana(player1, ManaColor.BLUE, 104);

        harness.castSorcery(player1, 0, 101, targets);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ancient Kavu");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(101);
    }
}
