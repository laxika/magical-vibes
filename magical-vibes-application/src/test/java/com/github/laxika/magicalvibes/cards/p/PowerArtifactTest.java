package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BasaltMonolith;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PowerArtifact.class, BasaltMonolith.class, GrizzlyBears.class})
class PowerArtifactTest extends BaseCardTest {

    @Test
    void stackedReductionsKeepOneManaMinimum() {
        Permanent artifact = castOnArtifact();
        castPowerArtifact(artifact);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Reduces the enchanted artifact's activated ability by two generic mana")
    void reducesEnchantedArtifactAbility() {
        Permanent artifact = castOnArtifact();
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not reduce another artifact's activated ability")
    void doesNotReduceAnotherArtifact() {
        Permanent enchantedArtifact = harness.addToBattlefieldAndReturn(player2, new BasaltMonolith());
        harness.addToBattlefield(player2, new BasaltMonolith());
        castPowerArtifact(enchantedArtifact);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Does not reduce the enchanted artifact's cost below one mana")
    void keepsOneManaMinimum() {
        Permanent artifact = castOnArtifact();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can enchant only an artifact")
    void cannotEnchantNonArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PowerArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    private Permanent castOnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BasaltMonolith());
        castPowerArtifact(artifact);
        return artifact;
    }

    private void castPowerArtifact(Permanent artifact) {
        harness.setHand(player1, List.of(new PowerArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();
    }
}
