package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BairdStewardOfArgiveTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Baird has static RequirePaymentToAttackEffect with cost 1")
    void hasCorrectEffects() {
        BairdStewardOfArgive card = new BairdStewardOfArgive();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(RequirePaymentToAttackEffect.class);
        RequirePaymentToAttackEffect tax = (RequirePaymentToAttackEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(tax.amountPerAttacker()).isEqualTo(1);
    }

    // ===== Attack tax: opponent must pay {1} per attacking creature =====

    @Test
    @DisplayName("Opponent can attack if they pay {1} per creature")
    void opponentCanAttackWithPayment() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(player2, List.of(0));

        // Mana was spent
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        // Creature is attacking
        Permanent bear = findPermanent(player2, "Grizzly Bears");
        assertThat(bear.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Opponent cannot attack without enough mana to pay the tax")
    void opponentCannotAttackWithoutPayment() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // No mana added — tax cannot be paid
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Tax scales with number of attackers — not enough mana")
    void taxScalesWithNumberOfAttackers() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Only 1 mana — can't pay for 2 attackers at {1} each
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Tax scales with number of attackers — enough mana for all")
    void canAttackWithMultipleCreaturesIfEnoughMana() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        List<Permanent> bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .toList();
        assertThat(bears).hasSize(2);
        assertThat(bears).allMatch(Permanent::isAttacking);
    }

    @Test
    @DisplayName("Opponent can choose to declare no attackers without paying")
    void opponentCanDeclineToAttack() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // No mana, but declaring 0 attackers is fine
        declareAttackers(player2, List.of());

        Permanent bear = findPermanent(player2, "Grizzly Bears");
        assertThat(bear.isAttacking()).isFalse();
    }

    // ===== Two Bairds stack =====

    @Test
    @DisplayName("Two Bairds stack — opponent must pay {2} per creature")
    void twoBairdsStack() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Only 1 mana — need 2 per creature with two Bairds
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    // ===== Tax removed when Baird leaves =====

    @Test
    @DisplayName("Tax is removed when Baird leaves the battlefield")
    void taxRemovedWhenBairdLeaves() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Remove Baird
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Baird, Steward of Argive"));

        // No mana needed — no tax
        declareAttackers(player2, List.of(0));

        Permanent bear = findPermanent(player2, "Grizzly Bears");
        assertThat(bear.isAttacking()).isTrue();
    }

    // ===== Helpers =====

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
