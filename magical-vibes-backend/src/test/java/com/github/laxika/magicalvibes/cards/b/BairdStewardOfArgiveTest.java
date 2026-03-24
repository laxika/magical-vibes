package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HierophantsChalice;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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

    // ===== Mana abilities during attacker declaration (CR 508.1i) =====

    @Test
    @DisplayName("Can tap land for mana during attacker declaration then declare with tax paid")
    void canTapLandDuringDeclarationThenDeclare() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addNonSickCreature(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(player2.getId());

        // Bears at index 0, Forest at index 1
        gs.tapPermanent(gd, player2, 1);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);

        gs.declareAttackers(gd, player2, List.of(0));

        Permanent bear = findPermanent(player2, "Grizzly Bears");
        assertThat(bear.isAttacking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Tapping insufficient mana then declaring too many attackers fails")
    void tapInsufficientManaForMultipleAttackersFails() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addNonSickCreature(player2, new GrizzlyBears());
        addNonSickCreature(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(player2.getId());

        // Bears at 0,1; Forest at index 2. Tap Forest for 1 mana — need 2 for both attackers
        gs.tapPermanent(gd, player2, 2);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Non-declarant cannot tap during opponent's attacker declaration")
    void nonDeclarantCannotTapDuringDeclaration() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        harness.addToBattlefield(player1, new Forest());
        addNonSickCreature(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(player2.getId());

        // player1 tries to tap their Forest — should fail (they're not the declarant)
        assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-mana activated ability is blocked during attacker declaration")
    void nonManaAbilityBlockedDuringDeclaration() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addNonSickCreature(player2, new ProdigalPyromancer());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(player2.getId());

        // Pyromancer's tap ability is not a mana ability — should be blocked
        assertThatThrownBy(() -> gs.activateAbility(gd, player2, 0, 0, null, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only mana abilities can be activated during attacker declaration");
    }

    @Test
    @DisplayName("Mana activated ability is allowed during attacker declaration")
    void manaAbilityAllowedDuringDeclaration() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addNonSickCreature(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HierophantsChalice());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(player2.getId());

        // Hierophant's Chalice mana ability (index 1 = chalice on battlefield) — should succeed
        gs.activateAbility(gd, player2, 1, 0, null, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Full flow: tap lands then attack with multiple creatures")
    void fullFlowTapThenAttackMultiple() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addNonSickCreature(player2, new GrizzlyBears());
        addNonSickCreature(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(player2.getId());

        // Bears at 0,1; Plains at 2,3
        gs.tapPermanent(gd, player2, 2);
        gs.tapPermanent(gd, player2, 3);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);

        gs.declareAttackers(gd, player2, List.of(0, 1));

        List<Permanent> bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .toList();
        assertThat(bears).hasSize(2);
        assertThat(bears).allMatch(Permanent::isAttacking);
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
