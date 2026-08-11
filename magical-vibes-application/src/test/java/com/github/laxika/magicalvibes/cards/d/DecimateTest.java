package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DecimateTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact, creature, enchantment, and land")
    void destroysOneOfEachRequiredType() {
        harness.addToBattlefield(player2, new Millstone());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Decimate()));
        addMana();

        harness.castSorcery(player1, 0, List.of(
                harness.getPermanentId(player2, "Millstone"),
                harness.getPermanentId(player2, "Grizzly Bears"),
                harness.getPermanentId(player2, "Glorious Anthem"),
                harness.getPermanentId(player2, "Forest")));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Millstone");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Allows one permanent to be chosen for multiple target types")
    void allowsSharedTargetsForOverlappingTypes() {
        harness.addToBattlefield(player2, new GoldMyr());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Decimate()));
        addMana();

        UUID artifactCreatureId = harness.getPermanentId(player2, "Gold Myr");
        harness.castSorcery(player1, 0, List.of(
                artifactCreatureId,
                artifactCreatureId,
                harness.getPermanentId(player2, "Glorious Anthem"),
                harness.getPermanentId(player2, "Forest")));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gold Myr");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Requires legal targets in every target position")
    void rejectsWrongTargetType() {
        harness.addToBattlefield(player2, new Millstone());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Decimate()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(
                harness.getPermanentId(player2, "Grizzly Bears"),
                harness.getPermanentId(player2, "Millstone"),
                harness.getPermanentId(player2, "Glorious Anthem"),
                harness.getPermanentId(player2, "Forest"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
