package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsWithChosenNameCantBeCastEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NevermoreTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Nevermore has ChooseCardNameOnEnterEffect excluding lands and static casting restriction")
    void hasCorrectEffects() {
        Nevermore card = new Nevermore();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ChooseCardNameOnEnterEffect.class);
        ChooseCardNameOnEnterEffect chooseEffect =
                (ChooseCardNameOnEnterEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(chooseEffect.excludedTypes()).containsExactly(CardType.LAND);

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(SpellsWithChosenNameCantBeCastEffect.class);
    }

    // ===== Casting and card name choice =====

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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Nevermore"));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.colorChoice().playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a card name sets chosenName on the permanent")
    void choosingNameSetsOnPermanent() {
        harness.setHand(player1, List.of(new Nevermore()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Nevermore"))
                .findFirst().orElseThrow();
        assertThat(perm.getChosenName()).isEqualTo("Grizzly Bears");
    }

    // ===== Static casting restriction =====

    @Test
    @DisplayName("Opponent cannot cast spells with the chosen name")
    void opponentCannotCastChosenName() {
        Permanent nevermore = addReadyNevermore(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Controller also cannot cast spells with the chosen name")
    void controllerCannotCastChosenName() {
        Permanent nevermore = addReadyNevermore(player1, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Spells with different names can still be cast")
    void spellsWithDifferentNamesCanStillBeCast() {
        Permanent nevermore = addReadyNevermore(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID nevermoreId = harness.getPermanentId(player1, "Nevermore");

        harness.castInstant(player2, 0, nevermoreId);

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Restriction lifts when source leaves =====

    @Test
    @DisplayName("Casting restriction lifts when Nevermore is destroyed")
    void castingRestrictionLiftsWhenDestroyed() {
        Permanent nevermore = addReadyNevermore(player1, "Grizzly Bears");

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
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Multiple Nevermores =====

    @Test
    @DisplayName("Multiple Nevermores can name different cards")
    void multipleNevermoresBlockDifferentCards() {
        addReadyNevermore(player1, "Grizzly Bears");
        addReadyNevermore(player1, "Naturalize");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("Nevermore with no chosen name does not block anything")
    void noChosenNameDoesNotBlock() {
        Nevermore card = new Nevermore();
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Helpers =====

    private Permanent addReadyNevermore(Player player, String chosenName) {
        Nevermore card = new Nevermore();
        Permanent perm = new Permanent(card);
        perm.setChosenName(chosenName);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
