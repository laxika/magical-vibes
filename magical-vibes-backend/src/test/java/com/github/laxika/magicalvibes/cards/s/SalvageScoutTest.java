package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SalvageScoutTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Salvage Scout has correct activated ability")
    void hasCorrectProperties() {
        SalvageScout card = new SalvageScout();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{W}");
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(2);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(1)).isInstanceOf(ReturnCardFromGraveyardEffect.class);
    }

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability sacrifices Salvage Scout and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        addScoutToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Salvage Scout"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Salvage Scout"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Salvage Scout");
    }

    @Test
    @DisplayName("Activating ability consumes {W} mana")
    void activatingAbilityConsumesMana() {
        addScoutToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    // ===== Resolution — returning artifact from graveyard =====

    @Test
    @DisplayName("Returns artifact from graveyard to hand")
    void returnsArtifactFromGraveyardToHand() {
        addScoutToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Leonin Scimitar");
        harness.assertNotInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Choosing specific artifact when multiple are in graveyard")
    void choosesSpecificArtifactFromGraveyard() {
        addScoutToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new LeoninScimitar(), new Spellbook()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose Spellbook (index 1)
        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertInHand(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    // ===== Non-artifact cards are not valid choices =====

    @Test
    @DisplayName("Cannot choose non-artifact card from graveyard")
    void cannotChooseNonArtifactFromGraveyard() {
        addScoutToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Index 0 is Grizzly Bears (creature, not artifact) — not a valid choice
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Salvage Scout itself is not a valid choice (it is a creature, not an artifact)")
    void scoutItselfNotValidChoice() {
        addScoutToBattlefield(player1);
        // Only non-artifact cards in graveyard — after sacrifice, only Salvage Scout is there
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        // Salvage Scout is now in graveyard after sacrifice
        harness.assertInGraveyard(player1, "Salvage Scout");

        harness.passBothPriorities();

        // No valid artifact cards in graveyard — ability resolves with no effect
        assertThat(gd.stack).isEmpty();
    }

    // ===== No tap required — can activate with summoning sickness and while tapped =====

    @Test
    @DisplayName("Can activate with summoning sickness since ability does not require tap")
    void canActivateWithSummoningSickness() {
        SalvageScout card = new SalvageScout();
        harness.addToBattlefield(player1, card);
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Can activate while tapped since ability does not require tap")
    void canActivateWhileTapped() {
        Permanent scout = addScoutToBattlefield(player1);
        scout.tap();
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    // ===== Validation — cannot activate =====

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addScoutToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Opponent cannot make graveyard choice =====

    @Test
    @DisplayName("Opponent cannot make graveyard choice for controller")
    void opponentCannotChoose() {
        addScoutToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    // ===== Stack is empty after full resolution =====

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        addScoutToBattlefield(player1);
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addScoutToBattlefield(Player player) {
        SalvageScout card = new SalvageScout();
        Permanent scout = new Permanent(card);
        scout.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(scout);
        return scout;
    }
}
