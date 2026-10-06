package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HulkBrutalBrawler.class, GrizzlyBears.class})
class HulkBrutalBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Hulk attacks each combat if able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new HulkBrutalBrawler());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Attacking puts a +1/+1 counter on each other creature you control")
    void attacksCounterOtherControlledCreatures() {
        Permanent hulk = addCreatureReady(player1, new HulkBrutalBrawler());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Tapped Hulk is not required to attack")
    void tappedHulkCanStayOutOfCombat() {
        Permanent hulk = addCreatureReady(player1, new HulkBrutalBrawler());
        hulk.tap();

        declareAttackers(List.of());

        assertThat(hulk.isAttacking()).isFalse();
        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Summoning-sick Hulk is not required to attack")
    void summoningSickHulkCanStayOutOfCombat() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new HulkBrutalBrawler());

        declareAttackers(List.of());

        assertThat(hulk.isAttacking()).isFalse();
        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Attack trigger includes creatures entering before resolution")
    void countersCreaturesPresentAtResolution() {
        Permanent hulk = addCreatureReady(player1, new HulkBrutalBrawler());
        Permanent firstAlly = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        Permanent newAlly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(hulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(firstAlly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(newAlly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
