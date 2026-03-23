package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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
        addNonSickCreature(player2, new GrizzlyBears());

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
        addNonSickCreature(player2, new GrizzlyBears());

        // No mana added — tax cannot be paid
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Tax scales with number of attackers — not enough mana")
    void taxScalesWithNumberOfAttackers() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addNonSickCreature(player2, new GrizzlyBears());
        addNonSickCreature(player2, new GrizzlyBears());

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
        addNonSickCreature(player2, new GrizzlyBears());
        addNonSickCreature(player2, new GrizzlyBears());

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
        addNonSickCreature(player2, new GrizzlyBears());

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
        addNonSickCreature(player2, new GrizzlyBears());

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
        addNonSickCreature(player2, new GrizzlyBears());

        // Remove Baird
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Baird, Steward of Argive"));

        // No mana needed — no tax; declareAttackers should not throw
        // (combat auto-resolves since player1 has no blockers, so isAttacking is cleared)
        declareAttackers(player2, List.of(0));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    // ===== State preservation on failed tax check (CombatAttackService fix) =====

    @Test
    @DisplayName("Failed tax check preserves ATTACKER_DECLARATION awaiting state")
    void failedTaxCheckPreservesAwaitingState() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addNonSickCreature(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");

        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.ATTACKER_DECLARATION)).isTrue();
    }

    @Test
    @DisplayName("Failed tax check does not mark any creatures as attacking")
    void failedTaxCheckDoesNotMarkAttackers() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addNonSickCreature(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        Permanent bear = findPermanent(player2, "Grizzly Bears");
        assertThat(bear.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Failed tax check does not deduct mana")
    void failedTaxCheckDoesNotDeductMana() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addNonSickCreature(player2, new GrizzlyBears());
        addNonSickCreature(player2, new GrizzlyBears());

        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    // ===== GameService re-sends AVAILABLE_ATTACKERS on failure =====

    @Test
    @DisplayName("Failed tax check re-sends AVAILABLE_ATTACKERS to the player")
    void failedTaxCheckResendsAvailableAttackers() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addNonSickCreature(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        harness.clearMessages();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(harness.getConn2().getMessagesContaining("AVAILABLE_ATTACKERS")).isNotEmpty();
    }

    @Test
    @DisplayName("Player can retry with fewer attackers after failed tax check")
    void canRetryWithFewerAttackersAfterFailedTaxCheck() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addNonSickCreature(player2, new GrizzlyBears());
        addNonSickCreature(player2, new GrizzlyBears());

        harness.addMana(player2, ManaColor.COLORLESS, 1);

        // First attempt: 2 attackers — fails
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);

        // Retry with 1 attacker — should succeed (state was preserved)
        gs.declareAttackers(gd, player2, List.of(0));

        Permanent bear = gd.playerBattlefields.get(player2.getId()).get(0);
        assertThat(bear.isAttacking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Helpers =====

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void addNonSickCreature(Player player, Card card) {
        Permanent p = new Permanent(card);
        p.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(p);
    }

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
