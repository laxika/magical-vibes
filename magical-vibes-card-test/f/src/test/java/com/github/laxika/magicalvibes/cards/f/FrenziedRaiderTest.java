package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BattershieldWarrior;
import com.github.laxika.magicalvibes.cards.w.WaterServant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrenziedRaider.class, BattershieldWarrior.class, WaterServant.class})
class FrenziedRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you activate a boast ability, Frenzied Raider gets a +1/+1 counter")
    void getsCounterWhenBoastAbilityIsActivated() {
        Permanent raider = addCreatureReady(player1, new FrenziedRaider());
        Permanent warrior = addCreatureReady(player1, new BattershieldWarrior());
        warrior.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(raider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Frenzied Raider does not trigger for a non-boast ability")
    void doesNotTriggerForNonBoastAbility() {
        Permanent raider = addCreatureReady(player1, new FrenziedRaider());
        addCreatureReady(player1, new WaterServant());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(raider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's boast activation does not give Frenzied Raider a counter")
    void doesNotTriggerForOpponentsBoastAbility() {
        Permanent raider = addCreatureReady(player1, new FrenziedRaider());
        Permanent warrior = addCreatureReady(player2, new BattershieldWarrior());
        warrior.setAttackedThisTurn(true);
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(raider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each boast activation gives every Frenzied Raider its own counter")
    void eachRaiderTriggersForEachBoastActivation() {
        Permanent firstRaider = addCreatureReady(player1, new FrenziedRaider());
        Permanent secondRaider = addCreatureReady(player1, new FrenziedRaider());
        Permanent firstWarrior = addCreatureReady(player1, new BattershieldWarrior());
        Permanent secondWarrior = addCreatureReady(player1, new BattershieldWarrior());
        firstWarrior.setAttackedThisTurn(true);
        secondWarrior.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 2, null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, 3, null, null);
        resolveAllTriggers();

        assertThat(firstRaider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(secondRaider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(firstWarrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(secondWarrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Raider's counter resolves before the boast ability and survives its source leaving")
    void triggerResolvesBeforeBoastEvenWhenBoastSourceLeaves() {
        Permanent raider = addCreatureReady(player1, new FrenziedRaider());
        Permanent warrior = addCreatureReady(player1, new BattershieldWarrior());
        warrior.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);

        assertThat(raider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(2);
        gd.playerBattlefields.get(player1.getId()).remove(warrior);
        gd.playerGraveyards.get(player1.getId()).add(warrior.getCard());
        harness.passBothPriorities();

        assertThat(raider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(raider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A pending counter cannot be placed on a new Raider after the original leaves")
    void pendingTriggerDoesNotPutCounterOnReplacementRaider() {
        Permanent original = addCreatureReady(player1, new FrenziedRaider());
        Permanent warrior = addCreatureReady(player1, new BattershieldWarrior());
        warrior.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerGraveyards.get(player1.getId()).add(original.getCard());
        Permanent replacement = addCreatureReady(player1, new FrenziedRaider());
        resolveAllTriggers();

        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
