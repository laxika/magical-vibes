package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.n.NaturalEnd;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RainOfThorns.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class, Millstone.class})
class RainOfThornsTest extends BaseCardTest {

    // Modes: 0 = destroy artifact, 1 = destroy enchantment, 2 = destroy land

    private void giveMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Artifact mode destroys target artifact")
    void artifactModeDestroysArtifact() {
        Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
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
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
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
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
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
        Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
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
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Millstone());
        harness.setHand(player1, List.of(new RainOfThorns()));
        giveMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 3,
                new int[]{0}, List.of(bears.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Choosing artifact and land modes leaves an unchosen enchantment intact")
    void twoModesResolveWithoutDestroyingUnchosenType() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new RainOfThorns()));
        giveMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 2},
                List.of(artifact.getId(), land.getId()), null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Millstone");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @CardUsed({NaturalEnd.class})
    @DisplayName("Other selected modes still resolve when the artifact target leaves the battlefield")
    void remainingTargetsResolveAfterOneBecomesIllegal() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new RainOfThorns()));
        harness.setHand(player2, List.of(new NaturalEnd()));
        giveMana();
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 1, 2},
                List.of(artifact.getId(), enchantment.getId(), land.getId()), null);
        harness.castAndResolveInstant(player2, 0, artifact.getId());
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Forest");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Millstone");
        harness.assertInGraveyard(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "Rain of Thorns");
    }

    @Test
    @DisplayName("Enchantment mode cannot target an artifact")
    void enchantmentModeRejectsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new RainOfThorns()));
        giveMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 3,
                new int[]{1}, List.of(artifact.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Land mode cannot target an enchantment")
    void landModeRejectsEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new RainOfThorns()));
        giveMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 3,
                new int[]{2}, List.of(enchantment.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }
}
