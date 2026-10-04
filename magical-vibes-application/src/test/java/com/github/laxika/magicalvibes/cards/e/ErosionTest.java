package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.o.Oasis;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Erosion.class, Oasis.class, BogImp.class, Disenchant.class})
class ErosionTest extends BaseCardTest {

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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BogImp());

        harness.setHand(player1, List.of(new Erosion()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
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

    @Test
    @DisplayName("Enchanted land's controller may pay {1} to save the land")
    void paysManaToSaveLand() {
        Permanent land = addLand(player2);
        attachErosion(land);

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities(); // resolve trigger -> prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player2, "Pay {1}");

        harness.assertOnBattlefield(player2, "Oasis");
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

        harness.assertOnBattlefield(player2, "Oasis");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Controller can choose life instead of mana when both payment alternatives are available")
    void choosesLifeWhenBothPaymentAlternativesAreAvailable() {
        Permanent land = addLand(player2);
        attachErosion(land);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player2, "Pay 1 life");

        harness.assertOnBattlefield(player2, "Oasis");
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

        harness.assertNotOnBattlefield(player2, "Oasis");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Controller can choose life instead of mana when both payment alternatives are available")
    void canChooseLifeInsteadOfManaWhenBothAreAvailable() {
        Permanent land = addLand(player2);
        attachErosion(land);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player2, "Pay 1 life");

        harness.assertOnBattlefield(player2, "Oasis");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Erosion does NOT trigger during the aura controller's own upkeep")
    void doesNotFireDuringAuraControllerUpkeep() {
        Permanent land = addLand(player2);
        attachErosion(land);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertOnBattlefield(player2, "Oasis");
    }

    @Test
    @DisplayName("Removing Erosion in response does not stop its upkeep ability")
    void removingAuraDoesNotStopUpkeepAbility() {
        Permanent land = addLand(player2);
        attachErosion(land);
        UUID erosionId = harness.getPermanentId(player1, "Erosion");

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castInstant(player2, 0, erosionId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Erosion");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Oasis");
        harness.assertInGraveyard(player2, "Oasis");
    }

    @Test
    @DisplayName("Controller may decline even when both payment alternatives are available")
    void declinesWhenBothPaymentsAreAvailable() {
        Permanent land = addLand(player2);
        attachErosion(land);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Don't pay");

        harness.assertNotOnBattlefield(player2, "Oasis");
        harness.assertInGraveyard(player2, "Oasis");
        harness.assertInGraveyard(player1, "Erosion");
        harness.assertLife(player2, lifeBefore);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Erosion triggers on its controller's upkeep when enchanting their own land")
    void triggersForOwnLand() {
        Permanent land = addLand(player1);
        attachErosion(land);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Oasis");
        harness.assertLife(player1, lifeBefore - 1);
    }

    private void attachErosion(Permanent land) {
        Permanent erosion = harness.addToBattlefieldAndReturn(player1, new Erosion());
        erosion.setAttachedTo(land.getId());
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Oasis());
    }

}
