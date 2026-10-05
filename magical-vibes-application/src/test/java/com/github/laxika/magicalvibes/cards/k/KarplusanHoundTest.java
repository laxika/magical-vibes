package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.c.ChandraBoldPyromancer;
import com.github.laxika.magicalvibes.cards.m.MesaUnicorn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KarplusanHound.class, ChandraBoldPyromancer.class, MesaUnicorn.class})
class KarplusanHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with a Chandra planeswalker on the battlefield queues target selection")
    void attackTriggerFiresWhenControllingChandra() {
        addCreatureReady(player1, new KarplusanHound());
        harness.enterBattlefieldAndReturn(player1, new ChandraBoldPyromancer());
        addCreatureReady(player2, new MesaUnicorn());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Deals 2 damage to chosen creature when Chandra is controlled")
    void deals2DamageToTargetCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new KarplusanHound());
        harness.enterBattlefieldAndReturn(player1, new ChandraBoldPyromancer());
        Permanent opponentCreature = addCreatureReady(player2, new MesaUnicorn());

        declareAttackers(player1, List.of(0));

        // Choose the opponent's creature as target
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Opponent's creature (2/2) should have taken 2 damage and died
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentCreature.getId()));
    }

    @Test
    @DisplayName("Deals 2 damage to target player when Chandra is controlled")
    void deals2DamageToTargetPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new KarplusanHound());
        harness.enterBattlefieldAndReturn(player1, new ChandraBoldPyromancer());

        declareAttackers(player1, List.of(0));

        // Choose the opponent player as target
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Opponent should have taken 2 trigger damage + 3 combat damage from the 3/3 Hound = 5 total
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Attacking without a Chandra planeswalker does NOT trigger the ability")
    void noTriggerWithoutChandra() {
        addCreatureReady(player1, new KarplusanHound());

        declareAttackers(player1, List.of(0));

        // No trigger should be on the stack
        assertThat(gd.stack).noneMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Karplusan Hound"));
        // No pending target selection
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Chandra does not enable the attack trigger")
    void opponentsChandraDoesNotEnableTrigger() {
        addCreatureReady(player1, new KarplusanHound());
        harness.enterBattlefieldAndReturn(player2, new ChandraBoldPyromancer());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).noneMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard() instanceof KarplusanHound);
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger does nothing if Chandra leaves before resolution")
    void conditionIsCheckedAgainOnResolution() {
        addCreatureReady(player1, new KarplusanHound());
        Permanent chandra = harness.enterBattlefieldAndReturn(player1, new ChandraBoldPyromancer());
        Permanent target = addCreatureReady(player2, new MesaUnicorn());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        chandra.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Chandra, Bold Pyromancer");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A replacement Chandra satisfies the condition when the trigger resolves")
    void replacementChandraEnablesResolution() {
        addCreatureReady(player1, new KarplusanHound());
        Permanent chandra = harness.enterBattlefieldAndReturn(player1, new ChandraBoldPyromancer());
        Permanent target = addCreatureReady(player2, new MesaUnicorn());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        chandra.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();
        harness.enterBattlefieldAndReturn(player1, new ChandraBoldPyromancer());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Mesa Unicorn");
    }

    @Test
    @DisplayName("The triggered damage still resolves after the Hound dies")
    void triggerResolvesAfterSourceDies() {
        Permanent hound = addCreatureReady(player1, new KarplusanHound());
        harness.enterBattlefieldAndReturn(player1, new ChandraBoldPyromancer());
        Permanent target = addCreatureReady(player2, new MesaUnicorn());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        hound.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Karplusan Hound");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Mesa Unicorn");
    }

    @Test
    @DisplayName("The attack trigger can deal damage to your own Chandra")
    void dealsDamageToPlaneswalker() {
        addCreatureReady(player1, new KarplusanHound());
        Permanent chandra = harness.enterBattlefieldAndReturn(player1, new ChandraBoldPyromancer());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("The attack trigger can damage its controller")
    void dealsDamageToController() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new KarplusanHound());
        harness.enterBattlefieldAndReturn(player1, new ChandraBoldPyromancer());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }
}
