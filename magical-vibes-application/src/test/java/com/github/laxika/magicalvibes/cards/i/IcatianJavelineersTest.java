package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IcatianJavelineersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with a javelin counter")
    void entersWithJavelinCounter() {
        harness.setHand(player1, List.of(new IcatianJavelineers()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent javelineers = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(javelineers.getCounterCount(CounterType.JAVELIN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 1 damage to a target player and removes its javelin counter")
    void dealsDamageAndRemovesCounter() {
        harness.setLife(player2, 20);
        Permanent javelineers = addReadyJavelineers();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(javelineers.isTapped()).isTrue();
        assertThat(javelineers.getCounterCount(CounterType.JAVELIN)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature")
    void dealsDamageToTargetCreature() {
        addReadyJavelineers();
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot activate without a javelin counter")
    void cannotActivateWithoutJavelinCounter() {
        Permanent javelineers = addReadyJavelineers();
        javelineers.setCounterCount(CounterType.JAVELIN, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyJavelineers() {
        Permanent javelineers = new Permanent(new IcatianJavelineers());
        javelineers.setSummoningSick(false);
        javelineers.setCounterCount(CounterType.JAVELIN, 1);
        gd.playerBattlefields.get(player1.getId()).add(javelineers);
        return javelineers;
    }
}
