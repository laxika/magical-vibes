package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PowerstoneShard;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.l.LlanowarScout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.h.HelmOfTheHost;
import com.github.laxika.magicalvibes.cards.t.TeferiHeroOfDominaria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BairdStewardOfArgive.class, BalothGorger.class, Forest.class, Plains.class,
        LlanowarScout.class, PowerstoneShard.class, HelmOfTheHost.class, TeferiHeroOfDominaria.class})
class BairdStewardOfArgiveTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent can attack if they pay {1} per creature")
    void opponentCanAttackWithPayment() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addCreatureReady(player2, new BalothGorger());

        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(player2, List.of(0));

        // Mana was spent
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        // Creature is attacking
        Permanent gorger = findPermanent(player2, "Baloth Gorger");
        assertThat(gorger.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Opponent cannot attack without enough mana to pay the tax")
    void opponentCannotAttackWithoutPayment() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addCreatureReady(player2, new BalothGorger());

        // No mana added — tax cannot be paid
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Tax scales with number of attackers — not enough mana")
    void taxScalesWithNumberOfAttackers() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addCreatureReady(player2, new BalothGorger());
        addCreatureReady(player2, new BalothGorger());

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
        addCreatureReady(player2, new BalothGorger());
        addCreatureReady(player2, new BalothGorger());

        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        List<Permanent> gorgers = findPermanents(player2, "Baloth Gorger");
        assertThat(gorgers).hasSize(2);
        assertThat(gorgers).allMatch(Permanent::isAttacking);
    }

    @Test
    @DisplayName("Opponent can choose to declare no attackers without paying")
    void opponentCanDeclineToAttack() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addCreatureReady(player2, new BalothGorger());

        // No mana, but declaring 0 attackers is fine
        declareAttackers(player2, List.of());

        Permanent gorger = findPermanent(player2, "Baloth Gorger");
        assertThat(gorger.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Baird and a nonlegendary Helm copy require {2} per attacker")
    void twoBairdsStack() {
        Permanent baird = addCreatureReady(player1, new BairdStewardOfArgive());
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new HelmOfTheHost());
        helm.setAttachedTo(baird.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Baird, Steward of Argive")).hasSize(2);
        addCreatureReady(player2, new BalothGorger());

        // Only 1 mana — need 2 per creature with two Bairds
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        gs.declareAttackers(gd, player2, List.of(0));
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(findPermanent(player2, "Baloth Gorger").isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Tax is removed when Baird leaves the battlefield")
    void taxRemovedWhenBairdLeaves() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addCreatureReady(player2, new BalothGorger());

        // Remove Baird
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Baird, Steward of Argive"));

        // No mana needed — no tax; declareAttackers should not throw
        // (combat auto-resolves since player1 has no blockers, so isAttacking is cleared)
        declareAttackers(player2, List.of(0));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Failed tax check preserves ATTACKER_DECLARATION awaiting state")
    void failedTaxCheckPreservesAwaitingState() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addCreatureReady(player2, new BalothGorger());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class) != null).isTrue();
    }

    @Test
    @DisplayName("Failed tax check does not mark any creatures as attacking")
    void failedTaxCheckDoesNotMarkAttackers() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addCreatureReady(player2, new BalothGorger());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        Permanent gorger = findPermanent(player2, "Baloth Gorger");
        assertThat(gorger.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Failed tax check does not deduct mana")
    void failedTaxCheckDoesNotDeductMana() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addCreatureReady(player2, new BalothGorger());
        addCreatureReady(player2, new BalothGorger());

        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Failed tax check re-sends AVAILABLE_ATTACKERS to the player")
    void failedTaxCheckResendsAvailableAttackers() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addCreatureReady(player2, new BalothGorger());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.clearMessages();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(harness.getConn2().getMessagesContaining("AVAILABLE_ATTACKERS")).isNotEmpty();
    }

    @Test
    @DisplayName("Player can retry with fewer attackers after failed tax check")
    void canRetryWithFewerAttackersAfterFailedTaxCheck() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addCreatureReady(player2, new BalothGorger());
        addCreatureReady(player2, new BalothGorger());

        harness.addMana(player2, ManaColor.COLORLESS, 1);

        // First attempt: 2 attackers — fails
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);

        // Retry with 1 attacker — should succeed (state was preserved)
        gs.declareAttackers(gd, player2, List.of(0));

        Permanent gorger = gd.playerBattlefields.get(player2.getId()).get(0);
        assertThat(gorger.isAttacking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can tap land for mana during attacker declaration then declare with tax paid")
    void canTapLandDuringDeclarationThenDeclare() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addCreatureReady(player2, new BalothGorger());
        harness.addToBattlefield(player2, new Forest());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Gorgers at index 0, Forest at index 1
        gs.tapPermanent(gd, player2, 1);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);

        gs.declareAttackers(gd, player2, List.of(0));

        Permanent gorger = findPermanent(player2, "Baloth Gorger");
        assertThat(gorger.isAttacking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Tapping insufficient mana then declaring too many attackers fails")
    void tapInsufficientManaForMultipleAttackersFails() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addCreatureReady(player2, new BalothGorger());
        addCreatureReady(player2, new BalothGorger());
        harness.addToBattlefield(player2, new Forest());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Gorgers at 0,1; Forest at index 2. Tap Forest for 1 mana — need 2 for both attackers
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
        addCreatureReady(player2, new BalothGorger());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // player1 tries to tap their Forest — should fail (they're not the declarant)
        assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-mana activated ability is blocked during attacker declaration")
    void nonManaAbilityBlockedDuringDeclaration() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addCreatureReady(player2, new LlanowarScout());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Scout's tap ability is not a mana ability — should be blocked
        assertThatThrownBy(() -> gs.activateAbility(gd, player2, 0, 0, null, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only mana abilities can be activated during attacker declaration");
    }

    @Test
    @DisplayName("Mana activated ability is allowed during attacker declaration")
    void manaAbilityAllowedDuringDeclaration() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addCreatureReady(player2, new BalothGorger());
        harness.addToBattlefield(player2, new PowerstoneShard());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Powerstone Shard mana ability (index 1 = shard on battlefield) — should succeed
        gs.activateAbility(gd, player2, 1, 0, null, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Full flow: tap lands then attack with multiple creatures")
    void fullFlowTapThenAttackMultiple() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        addCreatureReady(player2, new BalothGorger());
        addCreatureReady(player2, new BalothGorger());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Gorgers at 0,1; Plains at 2,3
        gs.tapPermanent(gd, player2, 2);
        gs.tapPermanent(gd, player2, 3);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);

        gs.declareAttackers(gd, player2, List.of(0, 1));

        List<Permanent> gorgers = findPermanents(player2, "Baloth Gorger");
        assertThat(gorgers).hasSize(2);
        assertThat(gorgers).allMatch(Permanent::isAttacking);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Tapped Baird still taxes creatures attacking his controller")
    void tappedBairdStillRequiresPayment() {
        Permanent baird = harness.addToBattlefieldAndReturn(player1, new BairdStewardOfArgive());
        baird.setTapped(true);
        addCreatureReady(player2, new BalothGorger());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Baird taxes creatures attacking a planeswalker his controller controls")
    void planeswalkerAttackRequiresPayment() {
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        Permanent teferi = harness.enterBattlefieldAndReturn(player1, new TeferiHeroOfDominaria());
        addCreatureReady(player2, new BalothGorger());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0), Map.of(0, teferi.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");

        harness.addMana(player2, ManaColor.WHITE, 1);
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, teferi.getId()));

        assertThat(findPermanent(player2, "Baloth Gorger").isAttacking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Tapped Baird still taxes attacks against his controller's planeswalkers")
    void tappedBairdStillProtectsPlaneswalkers() {
        Permanent baird = harness.addToBattlefieldAndReturn(player1, new BairdStewardOfArgive());
        baird.setTapped(true);
        Permanent teferi = harness.enterBattlefieldAndReturn(player1, new TeferiHeroOfDominaria());
        addCreatureReady(player2, new BalothGorger());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0), Map.of(0, teferi.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Baird does not tax his controller's attackers and attacks without tapping")
    void controllersAttackersAreNotTaxedAndBairdHasVigilance() {
        Permanent baird = addCreatureReady(player1, new BairdStewardOfArgive());
        Permanent gorger = addCreatureReady(player1, new BalothGorger());
        harness.addToBattlefield(player2, new BalothGorger());

        declareAttackers(player1, List.of(0, 1));

        assertThat(baird.isAttacking()).isTrue();
        assertThat(baird.isTapped()).isFalse();
        assertThat(gorger.isAttacking()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

}
