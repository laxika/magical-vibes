package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.k.KamahlPitFighter;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DelayingShield.class, KamahlPitFighter.class, DuskImp.class, DoublingSeason.class, WhiteKnight.class})
class DelayingShieldTest extends BaseCardTest {

    private Permanent shield() {
        return harness.addToBattlefieldAndReturn(player1, new DelayingShield());
    }

    private void dealThreeDamageTo(Player target) {
        Permanent kamahl = addCreatureReady(player2, new KamahlPitFighter());
        int kamahlIndex = gd.playerBattlefields.get(player2.getId()).indexOf(kamahl);
        harness.activateAbility(player2, kamahlIndex, null, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Damage to the controller becomes delay counters")
    void replacesDamageWithDelayCounters() {
        Permanent shield = shield();
        int lifeBefore = gd.getLife(player1.getId());

        dealThreeDamageTo(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining each upkeep payment causes one life loss per delay counter")
    void decliningPaymentsLosesLifePerCounter() {
        Permanent shield = shield();
        dealThreeDamageTo(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isZero();
    }

    @Test
    @DisplayName("Paying {1}{W} for each counter avoids all upkeep life loss")
    void payingEachCounterAvoidsLifeLoss() {
        Permanent shield = shield();
        dealThreeDamageTo(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("The shield only replaces damage to its controller")
    void doesNotReplaceDamageToAnotherPlayer() {
        Permanent shield = shield();
        dealThreeDamageTo(player2);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isZero();
    }

    @Test
    @DisplayName("Combat damage to the controller becomes delay counters")
    void replacesCombatDamageWithDelayCounters() {
        Permanent shield = shield();
        int lifeBefore = gd.getLife(player1.getId());
        Permanent attacker = addCreatureReady(player2, new DuskImp());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isEqualTo(2);
    }

    @Test
    void removesAllCountersBeforeOfferingPayments() {
        Permanent shield = shield();
        dealThreeDamageTo(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(shield.getCounterCount(CounterType.DELAY)).isZero();
        harness.assertLife(player1, 20);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertLife(player1, 17);
    }

    @Test
    void canPayForOnlySomeRemovedCounters() {
        Permanent shield = shield();
        dealThreeDamageTo(player1);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 18);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void upkeepWithoutCountersDoesNotOfferPayment() {
        Permanent shield = shield();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentUpkeepDoesNotRemoveCounters() {
        Permanent shield = shield();
        dealThreeDamageTo(player1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void controllerChoosesWhichShieldReplacesDamage() {
        Permanent first = shield();
        Permanent second = shield();

        dealThreeDamageTo(player1);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(first.getCounterCount(CounterType.DELAY)).isZero();
        assertThat(second.getCounterCount(CounterType.DELAY)).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    void chosenShieldTakesAllTheCounters() {
        Permanent first = shield();
        Permanent second = shield();

        dealThreeDamageTo(player1);
        chooseReplacement("Delaying Shield: put counters on it instead (2)");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(first.getCounterCount(CounterType.DELAY)).isZero();
        assertThat(second.getCounterCount(CounterType.DELAY)).isEqualTo(3);
        harness.assertLife(player1, 20);
    }

    @Test
    void preventionShieldAppliedFirstLeavesTheRestForTheChosenShield() {
        Permanent first = shield();
        Permanent second = shield();
        gd.playerDamagePreventionShields.put(player1.getId(), 2);

        dealThreeDamageTo(player1);
        assertThat(replacementChoice().options()).hasSize(3);
        chooseReplacement("Prevent the next 2 damage to you");
        assertThat(replacementChoice().options()).hasSize(2);
        chooseReplacement("Delaying Shield: put counters on it instead (1)");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(first.getCounterCount(CounterType.DELAY)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.DELAY)).isZero();
        assertThat(gd.playerDamagePreventionShields).doesNotContainKey(player1.getId());
        harness.assertLife(player1, 20);
    }

    @Test
    void shieldAppliedFirstLeavesThePreventionShieldUnused() {
        Permanent first = shield();
        gd.playerDamagePreventionShields.put(player1.getId(), 2);

        dealThreeDamageTo(player1);
        chooseReplacement("Delaying Shield");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(first.getCounterCount(CounterType.DELAY)).isEqualTo(3);
        assertThat(gd.playerDamagePreventionShields.get(player1.getId())).isEqualTo(2);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Combat damage from several attackers is one event whose counters all go on the chosen shield")
    void combatDamageFromSeveralAttackersGoesOnTheChosenShield() {
        Permanent first = shield();
        Permanent second = shield();
        addCreatureReady(player2, new DuskImp()).setAttacking(true);
        addCreatureReady(player2, new DuskImp()).setAttacking(true);

        resolveCombat(player2);
        assertThat(replacementChoice().options()).hasSize(2);
        chooseReplacement("Delaying Shield: put counters on it instead (1)");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(first.getCounterCount(CounterType.DELAY)).isEqualTo(4);
        assertThat(second.getCounterCount(CounterType.DELAY)).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("First-strike and regular combat damage are separate events, each with its own choice")
    void eachCombatDamageStepAsksAgain() {
        Permanent first = shield();
        Permanent second = shield();
        addCreatureReady(player2, new WhiteKnight()).setAttacking(true);
        addCreatureReady(player2, new DuskImp()).setAttacking(true);

        resolveCombat(player2);
        chooseReplacement("Delaying Shield: put counters on it instead (1)");
        assertThat(first.getCounterCount(CounterType.DELAY)).isEqualTo(2);
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        chooseReplacement("Delaying Shield: put counters on it instead (2)");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(first.getCounterCount(CounterType.DELAY)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.DELAY)).isEqualTo(2);
        harness.assertLife(player1, 20);
    }

    private PendingInteraction.ColorChoice replacementChoice() {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        return choice;
    }

    private void chooseReplacement(String labelPrefix) {
        harness.handleListChoice(player1, replacementChoice().options().stream()
                .filter(option -> option.startsWith(labelPrefix))
                .findFirst().orElseThrow());
    }

    @Test
    void delayCounterPlacementIsDoubledByDoublingSeason() {
        Permanent shield = shield();
        harness.addToBattlefield(player1, new DoublingSeason());

        dealThreeDamageTo(player1);

        harness.assertLife(player1, 20);
        assertThat(shield.getCounterCount(CounterType.DELAY)).isEqualTo(6);
    }

}
