package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YouthfulValkyrie.class, SerraAngel.class, BearCub.class})
class YouthfulValkyrieTest extends BaseCardTest {

    @Test
    @DisplayName("Another Angel entering under your control puts a +1/+1 counter on it")
    void anotherAngelEnteringPutsCounterOnIt() {
        Permanent valkyrie = harness.addToBattlefieldAndReturn(player1, new YouthfulValkyrie());
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(valkyrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Angel creature entering does not trigger it")
    void nonAngelEnteringDoesNotTrigger() {
        Permanent valkyrie = harness.addToBattlefieldAndReturn(player1, new YouthfulValkyrie());
        harness.setHand(player1, List.of(new BearCub()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(valkyrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Angel entering does not trigger it")
    void opponentAngelEnteringDoesNotTrigger() {
        Permanent valkyrie = harness.addToBattlefieldAndReturn(player1, new YouthfulValkyrie());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SerraAngel()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(valkyrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Youthful Valkyrie's own entry does not trigger it")
    void ownEntryDoesNotTrigger() {
        harness.setHand(player1, List.of(new YouthfulValkyrie()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent valkyrie = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(valkyrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another Youthful Valkyrie triggers only the Valkyrie already on the battlefield")
    void anotherValkyrieTriggersExistingValkyrie() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new YouthfulValkyrie());
        harness.setHand(player1, List.of(new YouthfulValkyrie()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent entering = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gd.stack).hasSize(1);
        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Angels entering without being cast each put a counter on every existing Valkyrie")
    void angelsEnteringWithoutBeingCastTriggerEachValkyrie() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new YouthfulValkyrie());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new YouthfulValkyrie());

        for (int i = 1; i <= 2; i++) {
            harness.enterBattlefieldAndReturn(player1, new SerraAngel());
            assertThat(gd.stack).hasSize(2);
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(i);
            assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(i);
            assertThat(gd.stack).isEmpty();
        }
    }
}
