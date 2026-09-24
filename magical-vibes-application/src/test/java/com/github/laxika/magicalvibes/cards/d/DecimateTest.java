package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Millikin;
import com.github.laxika.magicalvibes.cards.m.Mirari;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Decimate.class, Mirari.class, Millikin.class, DivineSacrament.class,
        DuskImp.class, Forest.class})
class DecimateTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact, creature, enchantment, and land")
    void destroysOneOfEachRequiredType() {
        harness.addToBattlefield(player2, new Mirari());
        harness.addToBattlefield(player2, new DuskImp());
        harness.addToBattlefield(player2, new DivineSacrament());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Decimate()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(
                harness.getPermanentId(player2, "Mirari"),
                harness.getPermanentId(player2, "Dusk Imp"),
                harness.getPermanentId(player2, "Divine Sacrament"),
                harness.getPermanentId(player2, "Forest")));

        harness.assertNotOnBattlefield(player2, "Mirari");
        harness.assertNotOnBattlefield(player2, "Dusk Imp");
        harness.assertNotOnBattlefield(player2, "Divine Sacrament");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Mirari");
        harness.assertInGraveyard(player2, "Dusk Imp");
        harness.assertInGraveyard(player2, "Divine Sacrament");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Allows one permanent to be chosen for multiple target types")
    void allowsSharedTargetsForOverlappingTypes() {
        harness.addToBattlefield(player2, new Millikin());
        harness.addToBattlefield(player2, new DivineSacrament());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Decimate()));
        addMana();

        UUID artifactCreatureId = harness.getPermanentId(player2, "Millikin");
        harness.castAndResolveSorcery(player1, 0, List.of(
                artifactCreatureId,
                artifactCreatureId,
                harness.getPermanentId(player2, "Divine Sacrament"),
                harness.getPermanentId(player2, "Forest")));

        harness.assertNotOnBattlefield(player2, "Millikin");
        harness.assertNotOnBattlefield(player2, "Divine Sacrament");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Requires legal targets in every target position")
    void rejectsWrongTargetType() {
        harness.addToBattlefield(player2, new Mirari());
        harness.addToBattlefield(player2, new DuskImp());
        harness.addToBattlefield(player2, new DivineSacrament());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Decimate()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(
                harness.getPermanentId(player2, "Dusk Imp"),
                harness.getPermanentId(player2, "Mirari"),
                harness.getPermanentId(player2, "Divine Sacrament"),
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
