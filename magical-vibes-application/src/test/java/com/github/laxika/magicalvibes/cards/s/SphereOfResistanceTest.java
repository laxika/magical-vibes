package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FightingChance;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SphereOfResistance.class, FightingChance.class, RagingGoblin.class})
class SphereOfResistanceTest extends BaseCardTest {

    @Test
    @DisplayName("Instant spells cost {1} more")
    void instantSpellsCostMore() {
        harness.addToBattlefield(player1, new SphereOfResistance());

        assertThatThrownBy(() -> harness.castFromHand(player1, new FightingChance(), "{R}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Instant spells are castable with enough mana to cover the increase")
    void instantSpellsAreCastableWithEnoughMana() {
        harness.addToBattlefield(player1, new SphereOfResistance());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromHand(player1, new FightingChance(), "{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Creature spells also cost {1} more")
    void creatureSpellsCostMore() {
        harness.addToBattlefield(player1, new SphereOfResistance());

        assertThatThrownBy(() -> harness.castFromHand(player1, new RagingGoblin(), "{R}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Artifact spells also cost {1} more")
    void artifactSpellsCostMore() {
        harness.addToBattlefield(player1, new SphereOfResistance());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, new SphereOfResistance(), "{2}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Multiple Spheres of Resistance increase the cost cumulatively")
    void multipleSpheresIncreaseCostCumulatively() {
        harness.addToBattlefield(player1, new SphereOfResistance());
        harness.addToBattlefield(player1, new SphereOfResistance());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFromHand(player1, new FightingChance(), "{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The cost increase is symmetric")
    void costIncreaseIsSymmetric() {
        harness.addToBattlefield(player1, new SphereOfResistance());
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromHand(player2, new FightingChance(), "{R}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A tapped Sphere still increases spell costs")
    void tappedSphereStillIncreasesCosts() {
        var sphere = harness.addToBattlefieldAndReturn(player1, new SphereOfResistance());
        sphere.tap();

        assertThatThrownBy(() -> harness.castFromHand(player1, new FightingChance(), "{R}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A Sphere on the stack does not increase spell costs")
    void sphereOnStackDoesNotIncreaseCosts() {
        harness.castFromHand(player1, new SphereOfResistance(), "{2}");

        harness.castFromHand(player1, new FightingChance(), "{R}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Creature spells can pay the increase with generic mana")
    void creatureSpellsPayGenericIncrease() {
        harness.addToBattlefield(player1, new SphereOfResistance());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, new RagingGoblin(), "{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent can pay the increase imposed by a Sphere")
    void opponentCanPayIncrease() {
        harness.addToBattlefield(player1, new SphereOfResistance());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player2, new FightingChance(), "{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
