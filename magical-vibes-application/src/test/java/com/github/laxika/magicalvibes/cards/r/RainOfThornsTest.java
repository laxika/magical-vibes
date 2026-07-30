package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RainOfThornsTest extends BaseCardTest {

    // Modes: 0 = destroy artifact, 1 = destroy enchantment, 2 = destroy land

    private void giveMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Artifact mode destroys target artifact")
    void artifactModeDestroysArtifact() {
        harness.addToBattlefield(player2, new Millstone());
        Permanent millstone = findPermanent(player2, "Millstone");
        harness.setHand(player1, List.of(new RainOfThorns()));
        giveMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0},
                List.of(millstone.getId()), null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    @DisplayName("Enchantment mode destroys target enchantment")
    void enchantmentModeDestroysEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        Permanent anthem = findPermanent(player2, "Glorious Anthem");
        harness.setHand(player1, List.of(new RainOfThorns()));
        giveMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1},
                List.of(anthem.getId()), null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Land mode destroys target land")
    void landModeDestroysLand() {
        harness.addToBattlefield(player2, new Forest());
        Permanent forest = findPermanent(player2, "Forest");
        harness.setHand(player1, List.of(new RainOfThorns()));
        giveMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{2},
                List.of(forest.getId()), null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("All three modes destroy all three permanents with no extra cost")
    void allThreeModesResolve() {
        harness.addToBattlefield(player2, new Millstone());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new Forest());
        Permanent millstone = findPermanent(player2, "Millstone");
        Permanent anthem = findPermanent(player2, "Glorious Anthem");
        Permanent forest = findPermanent(player2, "Forest");
        harness.setHand(player1, List.of(new RainOfThorns()));
        giveMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 1, 2},
                List.of(millstone.getId(), anthem.getId(), forest.getId()), null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Millstone");
        harness.assertInGraveyard(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Artifact mode cannot target a creature")
    void artifactModeCannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Millstone());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new RainOfThorns()));
        giveMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 3,
                new int[]{0}, List.of(bears.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }
}
