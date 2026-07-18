package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ErosionTest extends BaseCardTest {

    // ===== Targeting =====

    @Test
    @DisplayName("Can enchant a land with Erosion")
    void canEnchantLand() {
        Permanent land = addLand(player2);

        harness.setHand(player1, List.of(new Erosion()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, land.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a non-land creature")
    void cannotEnchantCreature() {
        addLand(player2); // a legal target exists so the Aura is playable
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new Erosion()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Resolving Erosion attaches it to the target land")
    void resolvingAttachesToLand() {
        Permanent land = addLand(player2);

        harness.setHand(player1, List.of(new Erosion()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Erosion")
                        && p.isAttached()
                        && p.getAttachedTo().equals(land.getId()));
    }

    // ===== Upkeep pay-or-destroy =====

    @Test
    @DisplayName("Enchanted land's controller may pay {1} to save the land")
    void paysManaToSaveLand() {
        Permanent land = addLand(player2);
        attachErosion(land);

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities(); // resolve trigger -> prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(landIsPresent(player2, land.getId())).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("With no mana, controller pays 1 life to save the land")
    void paysLifeToSaveLand() {
        Permanent land = addLand(player2);
        attachErosion(land);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger -> prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(landIsPresent(player2, land.getId())).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Declining the payment destroys the enchanted land")
    void decliningDestroysLand() {
        Permanent land = addLand(player2);
        attachErosion(land);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger -> prompt
        harness.handleMayAbilityChosen(player2, false);

        assertThat(landIsPresent(player2, land.getId())).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Erosion does NOT trigger during the aura controller's own upkeep")
    void doesNotFireDuringAuraControllerUpkeep() {
        Permanent land = addLand(player2);
        attachErosion(land);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(landIsPresent(player2, land.getId())).isTrue();
    }

    // ===== Helpers =====

    private void attachErosion(Permanent land) {
        Permanent erosion = new Permanent(new Erosion());
        erosion.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player1.getId()).add(erosion);
    }

    private Permanent addLand(Player player) {
        Permanent perm = new Permanent(new Forest());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private boolean landIsPresent(Player player, UUID landId) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(p -> p.getId().equals(landId));
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
