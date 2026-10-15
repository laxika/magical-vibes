package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.s.SulfurFalls;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({Nevermore.class, DarkthicketWolf.class, Naturalize.class, SulfurFalls.class})
class NevermoreTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Nevermore puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Nevermore()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Nevermore");
    }

    @Test
    @DisplayName("Resolving Nevermore awaits card name choice before entering battlefield")
    void resolvingTriggersCardNameChoice() {
        harness.setHand(player1, List.of(new Nevermore()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nevermore");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a card name sets chosenName on the permanent")
    void choosingNameSetsOnPermanent() {
        harness.setHand(player1, List.of(new Nevermore()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Darkthicket Wolf");

        Permanent perm = findPermanent(player1, "Nevermore");
        assertThat(perm.getChosenName()).isEqualTo("Darkthicket Wolf");
    }

    @Test
    @DisplayName("Opponent cannot cast spells with the chosen name")
    void opponentCannotCastChosenName() {
        addReadyNevermore(player1, "Darkthicket Wolf");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DarkthicketWolf()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Controller also cannot cast spells with the chosen name")
    void controllerCannotCastChosenName() {
        addReadyNevermore(player1, "Darkthicket Wolf");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DarkthicketWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Spells with different names can still be cast")
    void spellsWithDifferentNamesCanStillBeCast() {
        addReadyNevermore(player1, "Darkthicket Wolf");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID nevermoreId = harness.getPermanentId(player1, "Nevermore");

        harness.castInstant(player2, 0, nevermoreId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting restriction lifts when Nevermore is destroyed")
    void castingRestrictionLiftsWhenDestroyed() {
        addReadyNevermore(player1, "Darkthicket Wolf");

        // Destroy Nevermore
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID nevermoreId = harness.getPermanentId(player1, "Nevermore");
        harness.castInstant(player2, 0, nevermoreId);
        harness.passBothPriorities();

        // Restriction should be gone
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DarkthicketWolf()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Multiple Nevermores can name different cards")
    void multipleNevermoresBlockDifferentCards() {
        addReadyNevermore(player1, "Darkthicket Wolf");
        addReadyNevermore(player1, "Naturalize");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DarkthicketWolf()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Nevermore with no chosen name does not block anything")
    void noChosenNameDoesNotBlock() {
        harness.addToBattlefield(player1, new Nevermore());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DarkthicketWolf()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Land card names cannot be chosen")
    void cannotChooseLandName() {
        harness.setHand(player1, List.of(new Nevermore(), new SulfurFalls()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Sulfur Falls"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.assertNotOnBattlefield(player1, "Nevermore");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "Nevermore");
        harness.assertOnBattlefield(player1, "Nevermore");
    }

    @Test
    @DisplayName("A name that is not an Oracle card name cannot be chosen")
    void cannotChooseInventedName() {
        harness.setHand(player1, List.of(new Nevermore()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Nevermore Is Not A Real Card Name"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.assertNotOnBattlefield(player1, "Nevermore");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Naming Nevermore does not prevent its entry but prevents another copy being cast")
    void canNameItself() {
        harness.setHand(player1, List.of(new Nevermore(), new Nevermore()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Nevermore");

        harness.assertOnBattlefield(player1, "Nevermore");
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Named instant spells cannot be cast")
    void cannotCastNamedInstant() {
        Permanent nevermore = addReadyNevermore(player1, "Naturalize");
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nevermore.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Abilities of an existing permanent with the chosen name still work")
    void namedPermanentCanStillActivateAbilities() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());
        addReadyNevermore(player1, "Darkthicket Wolf");
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getEffectivePower()).isEqualTo(4);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Darkthicket Wolf");
    }

    private Permanent addReadyNevermore(Player player, String chosenName) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Nevermore());
        perm.setChosenName(chosenName);
        return perm;
    }
}
