package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
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

class TragicPoetTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Tragic Poet has correct activated ability")
    void hasCorrectProperties() {
        TragicPoet card = new TragicPoet();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(2);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(1)).isInstanceOf(ReturnCardFromGraveyardEffect.class);
    }

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability sacrifices Tragic Poet and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        addReadyPoet(player1);
        harness.setGraveyard(player1, List.of(new AuraOfSilence()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Tragic Poet"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tragic Poet"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Tragic Poet");
    }

    // ===== Resolution — returning enchantment from graveyard =====

    @Test
    @DisplayName("Returns enchantment from graveyard to hand")
    void returnsEnchantmentFromGraveyardToHand() {
        addReadyPoet(player1);
        harness.setGraveyard(player1, List.of(new AuraOfSilence()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Aura of Silence");
        harness.assertNotInGraveyard(player1, "Aura of Silence");
    }

    @Test
    @DisplayName("Choosing specific enchantment when multiple are in graveyard")
    void choosesSpecificEnchantmentFromGraveyard() {
        addReadyPoet(player1);
        harness.setGraveyard(player1, List.of(new AuraOfSilence(), new HonorOfThePure()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose Honor of the Pure (index 1)
        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertInHand(player1, "Honor of the Pure");
        harness.assertInGraveyard(player1, "Aura of Silence");
    }

    // ===== Non-enchantment cards are not valid choices =====

    @Test
    @DisplayName("Cannot choose non-enchantment card from graveyard")
    void cannotChooseNonEnchantmentFromGraveyard() {
        addReadyPoet(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new AuraOfSilence()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Index 0 is Grizzly Bears (creature, not enchantment) — not a valid choice
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Tragic Poet itself is not a valid choice (it is a creature, not an enchantment)")
    void poetItselfNotValidChoice() {
        addReadyPoet(player1);
        // Empty graveyard initially — only Tragic Poet will be there after sacrifice
        harness.setGraveyard(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        // Tragic Poet is now in graveyard after sacrifice
        harness.assertInGraveyard(player1, "Tragic Poet");

        harness.passBothPriorities();

        // No valid enchantment cards in graveyard — ability resolves with no effect
        assertThat(gd.stack).isEmpty();
    }

    // ===== Validation — cannot activate =====

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent poet = addReadyPoet(player1);
        poet.tap();
        harness.setGraveyard(player1, List.of(new AuraOfSilence()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        TragicPoet card = new TragicPoet();
        harness.addToBattlefield(player1, card);
        harness.setGraveyard(player1, List.of(new AuraOfSilence()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    // ===== Opponent cannot make graveyard choice =====

    @Test
    @DisplayName("Opponent cannot make graveyard choice for controller")
    void opponentCannotChoose() {
        addReadyPoet(player1);
        harness.setGraveyard(player1, List.of(new AuraOfSilence()));
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
        addReadyPoet(player1);
        harness.setGraveyard(player1, List.of(new AuraOfSilence()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addReadyPoet(Player player) {
        TragicPoet card = new TragicPoet();
        Permanent poet = new Permanent(card);
        poet.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(poet);
        return poet;
    }
}
