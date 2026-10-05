package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LodestoneGolem.class, GrizzlyBears.class, Juggernaut.class, LightningBolt.class})
class LodestoneGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Nonartifact creature spells cost {1} more")
    void nonartifactCreatureSpellsCostMore() {
        harness.addToBattlefield(player1, new LodestoneGolem());
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Artifact creature spells are not affected")
    void artifactCreatureSpellsAreNotAffected() {
        harness.addToBattlefield(player1, new LodestoneGolem());
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Juggernaut()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Juggernaut");
    }

    @Test
    @DisplayName("Two Lodestone Golems stack their cost increase")
    void costIncreasesStack() {
        harness.addToBattlefield(player1, new LodestoneGolem());
        harness.addToBattlefield(player2, new LodestoneGolem());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Controller can pay the generic tax with colorless mana")
    void controllerPaysGenericTax() {
        harness.addToBattlefield(player1, new LodestoneGolem());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Opponent's instant spells require and consume the generic tax")
    void opponentPaysInstantTax() {
        harness.addToBattlefield(player1, new LodestoneGolem());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Two Golems require exactly two additional generic mana")
    void paysBothGolemsTaxes() {
        harness.addToBattlefield(player1, new LodestoneGolem());
        harness.addToBattlefield(player2, new LodestoneGolem());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The tax ends when the Golem leaves the battlefield")
    void taxEndsWhenGolemDies() {
        harness.addToBattlefield(player1, new LodestoneGolem());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Lodestone Golem"));

        harness.assertNotOnBattlefield(player1, "Lodestone Golem");
        harness.assertInGraveyard(player1, "Lodestone Golem");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A Golem on the stack does not increase spell costs")
    void golemOnStackDoesNotTaxSpells() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new LodestoneGolem(), new LightningBolt()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
