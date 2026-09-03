package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({Wanderlust.class, GrizzlyBears.class})
class WanderlustTest extends BaseCardTest {

    // ===== Targeting =====

    @Test
    @DisplayName("Can enchant a creature with Wanderlust")
    void canEnchantCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Wanderlust()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @CardUsed(Forest.class)
    @DisplayName("Cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // a legal target exists so the Aura is playable
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new Wanderlust()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Upkeep damage =====

    @Test
    @DisplayName("Enchanted creature's controller takes 1 damage at their upkeep")
    void enchantedControllerTakesDamageAtUpkeep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachWanderlust(bears);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Wanderlust does NOT damage the aura controller during their own upkeep")
    void doesNotFireDuringAuraControllerUpkeep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachWanderlust(bears);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Damage accumulates over multiple upkeeps")
    void damageAccumulatesOverUpkeeps() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachWanderlust(bears);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void resolvingAttachesToTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Wanderlust()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Wanderlust)
                .findFirst()
                .orElseThrow();
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    void damagesAuraControllerWhenTheyControlEnchantedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachWanderlust(bears);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    // ===== Helpers =====

    private void attachWanderlust(Permanent creature) {
        Permanent wanderlust = new Permanent(new Wanderlust());
        wanderlust.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(wanderlust);
    }
}
