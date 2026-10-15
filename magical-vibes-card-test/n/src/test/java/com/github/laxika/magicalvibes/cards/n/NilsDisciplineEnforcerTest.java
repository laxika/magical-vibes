package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GideonChampionOfJustice;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NilsDisciplineEnforcer.class, GrizzlyBears.class, GideonChampionOfJustice.class})
class NilsDisciplineEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of the controller's end step, puts a counter on up to one creature per player")
    void putsCountersOnUpToOneCreaturePerPlayer() {
        harness.addToBattlefield(player1, new NilsDisciplineEnforcer());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        beginEndStep(player1);

        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.validIds()).contains(ownCreature.getId(), opposingCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());

        PendingInteraction.PermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.validIds()).contains(opposingCreature.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature with counters costs that many mana to attack the controller")
    void countersOnAttackerRequirePayment() {
        harness.addToBattlefield(player2, new NilsDisciplineEnforcer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The counter tax also applies when attacking a planeswalker")
    void countersOnAttackerRequirePaymentToAttackPlaneswalker() {
        harness.addToBattlefield(player2, new NilsDisciplineEnforcer());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GideonChampionOfJustice());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        beginAttack(player1);
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A creature with counters cannot attack without enough mana to pay")
    void cannotAttackWithoutPayment() {
        harness.addToBattlefield(player2, new NilsDisciplineEnforcer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax (2 required)");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    private void beginAttack(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    @Test
    void mayChooseNoCreaturesAtEndStep() {
        Permanent nils = harness.addToBattlefieldAndReturn(player1, new NilsDisciplineEnforcer());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        beginEndStep(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(nils.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mayChooseOnlyOwnCreatureAndCannotChooseAnotherWithSameController() {
        Permanent nils = harness.addToBattlefieldAndReturn(player1, new NilsDisciplineEnforcer());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        beginEndStep(player1);
        harness.handlePermanentChosen(player1, nils.getId());
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opposingCreature.getId())
                .doesNotContain(nils.getId(), ownCreature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(nils.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerOnOpponentsEndStep() {
        Permanent nils = harness.addToBattlefieldAndReturn(player1, new NilsDisciplineEnforcer());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        beginEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(nils.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void taxCountsCountersOfDifferentTypesTogether() {
        harness.addToBattlefield(player2, new NilsDisciplineEnforcer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.CHARGE, 2);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        declareAttackers(List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void eachAttackerPaysForItsOwnCounters() {
        harness.addToBattlefield(player2, new NilsDisciplineEnforcer());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setCounterCount(CounterType.CHARGE, 1);
        secondAttacker.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        declareAttackers(List.of(0, 1));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void counterTaxEndsWhenNilsLeavesBattlefield() {
        Permanent nils = harness.addToBattlefieldAndReturn(player2, new NilsDisciplineEnforcer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.CHARGE, 2);
        gd.playerBattlefields.get(player2.getId()).remove(nils);
        gd.playerGraveyards.get(player2.getId()).add(nils.getCard());

        declareAttackers(List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void creaturesWithoutCountersMayAttackWithoutPayment() {
        harness.addToBattlefield(player2, new NilsDisciplineEnforcer());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void ownNilsDoesNotTaxAttacksAgainstOpponent() {
        Permanent attacker = addCreatureReady(player1, new NilsDisciplineEnforcer());
        attacker.setCounterCount(CounterType.CHARGE, 2);

        declareAttackers(List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void remainingLegalTargetReceivesCounterWhenOtherTargetLeavesBattlefield() {
        harness.addToBattlefield(player1, new NilsDisciplineEnforcer());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        beginEndStep(player1);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(opposingCreature);
        gd.playerGraveyards.get(player2.getId()).add(opposingCreature.getCard());
        resolveAllTriggers();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void beginEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
