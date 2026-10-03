package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({CarnifexDemon.class, ContagiousNim.class, DarksteelAxe.class})
class CarnifexDemonTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two -1/-1 counters (6/6 becomes 4/4)")
    void entersWithTwoMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CarnifexDemon()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        Permanent demon = findDemon(player1);

        assertThat(demon.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(demon.getEffectivePower()).isEqualTo(4);
        assertThat(demon.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activated ability puts -1/-1 counter on each other creature")
    void abilityPutsCountersOnOtherCreatures() {
        addReadyDemon(player1);
        harness.addToBattlefield(player1, new ContagiousNim());
        harness.addToBattlefield(player2, new ContagiousNim());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Own Contagious Nim gets a -1/-1 counter
        Permanent ownNim = findPermanent(player1, "Contagious Nim");
        assertThat(ownNim.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        // Opponent's Contagious Nim also gets a -1/-1 counter
        Permanent opponentNim = findPermanent(player2, "Contagious Nim");
        assertThat(opponentNim.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability does NOT put counter on Carnifex Demon itself")
    void abilityDoesNotAffectSelf() {
        Permanent demon = addReadyDemon(player1);
        harness.addToBattlefield(player1, new ContagiousNim());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Demon started with 2 counters, had 1 removed as cost = 1 remaining
        // Should NOT have gotten an additional counter from its own effect
        assertThat(demon.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability removes a -1/-1 counter from source as cost")
    void abilityRemovesCounterFromSource() {
        Permanent demon = addReadyDemon(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Started with 2 -1/-1 counters, removed 1 as cost
        assertThat(demon.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(demon.getEffectivePower()).isEqualTo(5);
        assertThat(demon.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can activate ability twice to remove both counters")
    void canActivateTwice() {
        Permanent demon = addReadyDemon(player1);
        harness.addToBattlefield(player2, new ContagiousNim());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // First activation
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(demon.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        // Second activation
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(demon.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
        assertThat(demon.getEffectivePower()).isEqualTo(6);
        assertThat(demon.getEffectiveToughness()).isEqualTo(6);

        // Opponent's Contagious Nim (2/2) died from two -1/-1 counters (0/0 toughness)
        harness.assertNotOnBattlefield(player2, "Contagious Nim");
        harness.assertInGraveyard(player2, "Contagious Nim");
    }

    @Test
    @DisplayName("Cannot activate ability when no counters remain")
    void cannotActivateWithoutCounters() {
        Permanent demon = addReadyDemon(player1);
        demon.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Can activate ability during combat (instant speed)")
    void canActivateDuringCombat() {
        addReadyDemon(player1);
        harness.addToBattlefield(player2, new ContagiousNim());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent opponentNim = findPermanent(player2, "Contagious Nim");
        assertThat(opponentNim.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-creature permanents are not affected by the ability")
    void nonCreaturePermanentsNotAffected() {
        addReadyDemon(player1);
        harness.addToBattlefield(player1, new DarksteelAxe());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent axe = findPermanent(player1, "Darksteel Axe");
        assertThat(axe.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Counter removal is paid immediately, before other creatures get counters")
    void paysCounterCostBeforeResolution() {
        Permanent demon = addReadyDemon(player1);
        Permanent nim = harness.addToBattlefieldAndReturn(player2, new ContagiousNim());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(demon.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(nim.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();

        harness.passBothPriorities();
        assertThat(nim.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Another Carnifex Demon is affected even though the source is excluded")
    void affectsAnotherDemon() {
        Permanent source = addReadyDemon(player1);
        Permanent other = addReadyDemon(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @CardUsed({CarnifexDemon.class, Cloudshift.class})
    @DisplayName("A returned source is a different creature and receives a counter")
    void affectsSourceThatLeftAndReturnedBeforeResolution() {
        Permanent source = addReadyDemon(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);

        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, source.getId());
        harness.passBothPriorities();

        Permanent returned = findDemon(player1);
        assertThat(returned.getId()).isNotEqualTo(source.getId());
        // Resolve the original activation and any incorrectly queued entry trigger separately.
        for (int i = 0; i < 2 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.stack).isEmpty();
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    private Permanent addReadyDemon(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CarnifexDemon());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        return perm;
    }

    private Permanent findDemon(Player player) {
        return findPermanent(player, "Carnifex Demon");
    }
}
