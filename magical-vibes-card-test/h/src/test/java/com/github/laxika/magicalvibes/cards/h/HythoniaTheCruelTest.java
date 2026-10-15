package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.PharikasMender;
import com.github.laxika.magicalvibes.cards.s.SpearOfHeliod;
import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HythoniaTheCruel.class, TravelingPhilosopher.class, PharikasMender.class, SpearOfHeliod.class})
class HythoniaTheCruelTest extends BaseCardTest {

    @Test
    @DisplayName("When Hythonia becomes monstrous, it destroys all non-Gorgon creatures")
    void becomingMonstrousDestroysNonGorgonCreatures() {
        Permanent hythonia = addReadyHythonia();
        Permanent friendlyBear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        Permanent gorgon = harness.addToBattlefieldAndReturn(player2, new PharikasMender());
        Permanent spear = harness.addToBattlefieldAndReturn(player2, new SpearOfHeliod());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(friendlyBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingBear);
        harness.passBothPriorities();

        assertThat(hythonia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hythonia.isMonstrous()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hythonia).doesNotContain(friendlyBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(gorgon, spear).doesNotContain(opposingBear);
        harness.assertInGraveyard(player1, "Traveling Philosopher");
        harness.assertInGraveyard(player2, "Traveling Philosopher");
    }

    @Test
    @DisplayName("Already monstrous Hythonia can activate monstrosity without adding counters or destroying creatures")
    void alreadyMonstrousActivationDoesNothing() {
        Permanent hythonia = addReadyHythonia();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(hythonia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hythonia.isMonstrous()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(survivor);
    }

    @Test
    @DisplayName("Two stacked monstrosity activations produce counters and the destruction trigger only once")
    void stackedActivationsOnlyBecomeMonstrousOnce() {
        Permanent hythonia = addReadyHythonia();
        addMonstrosityMana();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(hythonia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.passBothPriorities();

        assertThat(hythonia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(survivor);
    }

    @Test
    @DisplayName("Summoning sickness does not prevent Hythonia from becoming monstrous")
    void summoningSickHythoniaCanBecomeMonstrous() {
        Permanent hythonia = harness.addToBattlefieldAndReturn(player1, new HythoniaTheCruel());
        hythonia.setSummoningSick(true);
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hythonia.isMonstrous()).isTrue();
        assertThat(hythonia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private Permanent addReadyHythonia() {
        Permanent hythonia = harness.addToBattlefieldAndReturn(player1, new HythoniaTheCruel());
        hythonia.setSummoningSick(false);
        return hythonia;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
