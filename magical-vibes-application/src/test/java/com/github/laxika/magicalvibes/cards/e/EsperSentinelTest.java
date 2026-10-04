package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SealOfRemoval;
import com.github.laxika.magicalvibes.cards.s.ShatteredEgo;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EsperSentinel.class, GrizzlyBears.class, HonorOfThePure.class, MindStone.class,
        SealOfRemoval.class, ShatteredEgo.class})
class EsperSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers on an opponent's first noncreature spell each turn")
    void triggersOnFirstOpponentNoncreatureSpell() {
        harness.addToBattlefield(player1, new EsperSentinel());
        prepareOpponentTurn();

        harness.setHand(player2, List.of(new GrizzlyBears(), new MindStone(), new MindStone()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.castArtifact(player2, 0);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.castArtifact(player2, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("Uses Esper Sentinel's power as the payment amount")
    void usesPowerAsPaymentAmount() {
        harness.addToBattlefield(player1, new HonorOfThePure());
        harness.addToBattlefield(player1, new EsperSentinel());
        prepareOpponentTurn();

        harness.setHand(player2, List.of(new MindStone()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Draws automatically when the opponent cannot pay Sentinel's power")
    void drawsWhenOpponentCannotPayPower() {
        harness.addToBattlefield(player1, new HonorOfThePure());
        harness.addToBattlefield(player1, new EsperSentinel());
        prepareOpponentTurn();

        harness.setHand(player2, List.of(new MindStone()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Declining payment causes a mandatory draw")
    void drawsAutomaticallyWhenOpponentDeclinesPayment() {
        harness.addToBattlefield(player1, new EsperSentinel());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new SealOfRemoval()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller's own noncreature spell does not trigger Sentinel")
    void doesNotTriggerOnControllersSpell() {
        harness.addToBattlefield(player1, new EsperSentinel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SealOfRemoval()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("A first spell cast before Sentinel enters still counts")
    void doesNotTriggerAfterOpponentsFirstSpellWasAlreadyCast() {
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new SealOfRemoval(), new SealOfRemoval()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new EsperSentinel());

        harness.castEnchantment(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Payment uses power at resolution after power changes")
    void usesPowerAtResolution() {
        harness.addToBattlefield(player1, new EsperSentinel());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new SealOfRemoval()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castEnchantment(player2, 0);
        harness.enterBattlefieldAndReturn(player1, new HonorOfThePure());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Removal in response preserves Sentinel's last known boosted power")
    void usesLastKnownPowerAfterLeavingBattlefield() {
        harness.addToBattlefield(player1, new HonorOfThePure());
        var sentinel = harness.addToBattlefieldAndReturn(player1, new EsperSentinel());
        harness.addToBattlefield(player1, new SealOfRemoval());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new SealOfRemoval()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castEnchantment(player2, 0);

        harness.activateAbility(player1, 2, null, sentinel.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Esper Sentinel");
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Negative power allows the opponent to pay zero")
    void canPayZeroWhenSentinelHasNegativePower() {
        var sentinel = harness.addToBattlefieldAndReturn(player1, new EsperSentinel());
        var aura = harness.addToBattlefieldAndReturn(player1, new ShatteredEgo());
        aura.setAttachedTo(sentinel.getId());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new SealOfRemoval()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("The opponent may decline a zero payment, causing a mandatory draw")
    void drawsWhenOpponentDeclinesZeroPayment() {
        var sentinel = harness.addToBattlefieldAndReturn(player1, new EsperSentinel());
        var aura = harness.addToBattlefieldAndReturn(player1, new ShatteredEgo());
        aura.setAttachedTo(sentinel.getId());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new SealOfRemoval()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Each Sentinel triggers independently on the first noncreature spell")
    void multipleSentinelsRequireSeparatePayments() {
        harness.addToBattlefield(player1, new EsperSentinel());
        harness.addToBattlefield(player1, new EsperSentinel());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new SealOfRemoval()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castEnchantment(player2, 0);
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("The first noncreature spell triggers again on a later turn")
    void firstSpellCountResetsEachTurn() {
        harness.addToBattlefield(player1, new EsperSentinel());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new SealOfRemoval()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new SealOfRemoval()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castEnchantment(player2, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
