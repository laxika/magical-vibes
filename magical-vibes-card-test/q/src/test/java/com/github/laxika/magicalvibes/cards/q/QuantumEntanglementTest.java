package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.w.WarriorsOfWakanda;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuantumEntanglement.class, WarriorsOfWakanda.class})
class QuantumEntanglementTest extends BaseCardTest {

    @Test
    @DisplayName("Pays first, then flickers the targeted creature through a separate entry trigger")
    void flickersTargetOnEntry() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WarriorsOfWakanda());
        creature.tap();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        castQuantumEntanglement(2);
        beginEntryPayment();
        payAndChooseCreature(creature);

        assertThat(findPermanent(player1, "Warriors of Wakanda").getId()).isEqualTo(creature.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Warriors of Wakanda");
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the entry payment needs no target and leaves the creature unchanged")
    void decliningEntryPaymentDoesNothing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WarriorsOfWakanda());
        castQuantumEntanglement(2);
        beginEntryPayment();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Warriors of Wakanda").getId()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Pays to flicker a creature at the beginning of the controller's end step")
    void flickersTargetAtEndStep() {
        harness.addToBattlefield(player1, new QuantumEntanglement());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WarriorsOfWakanda());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        payAndChooseCreature(creature);

        assertThat(findPermanent(player1, "Warriors of Wakanda").getId()).isEqualTo(creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(findPermanent(player1, "Warriors of Wakanda").getId()).isNotEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The paid reflexive ability cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new WarriorsOfWakanda());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new WarriorsOfWakanda());
        castQuantumEntanglement(2);
        beginEntryPayment();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, own.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Warriors of Wakanda").getId()).isEqualTo(opponent.getId());
        assertThat(findPermanent(player1, "Warriors of Wakanda").getId()).isNotEqualTo(own.getId());
    }

    @Test
    @DisplayName("The entry payment is offered even without a creature to target")
    void canPayWithoutCreatures() {
        castQuantumEntanglement(2);
        beginEntryPayment();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Quantum Entanglement");
    }

    @Test
    @DisplayName("Declining the end-step payment creates no targeted ability")
    void decliningEndStepPaymentDoesNothing() {
        harness.addToBattlefield(player1, new QuantumEntanglement());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WarriorsOfWakanda());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Warriors of Wakanda").getId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerAtOpponentEndStep() {
        harness.addToBattlefield(player1, new QuantumEntanglement());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WarriorsOfWakanda());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Warriors of Wakanda").getId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("A creature controlled by the enchantment's controller returns to its owner")
    void returnsStolenCreatureToOwner() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WarriorsOfWakanda());
        gd.stolenCreatures.put(creature.getId(), player2.getId());
        castQuantumEntanglement(2);
        beginEntryPayment();
        payAndChooseCreature(creature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Warriors of Wakanda");
        assertThat(findPermanent(player2, "Warriors of Wakanda").getId()).isNotEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Flash allows entry during an opponent's turn")
    void canEnterDuringOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        castQuantumEntanglement(1);
        beginEntryPayment();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Quantum Entanglement");
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
    }

    private void beginEntryPayment() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void payAndChooseCreature(Permanent creature) {
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(creature.getId());
    }

    private void castQuantumEntanglement(int manaOfEachColor) {
        harness.addMana(player1, ManaColor.WHITE, manaOfEachColor - 1);
        harness.addMana(player1, ManaColor.COLORLESS, manaOfEachColor - 1);
        harness.castFromHand(player1, new QuantumEntanglement(), "{1}{W}");
    }
}
