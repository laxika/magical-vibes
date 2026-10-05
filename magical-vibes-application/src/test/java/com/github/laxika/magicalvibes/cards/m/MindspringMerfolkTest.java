package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Mindspring Merfolk")
@CardUsed({MindspringMerfolk.class, CoralMerfolk.class, GrizzlyBears.class, Forest.class})
class MindspringMerfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Exhaust draws X and puts counters on each Merfolk creature you control")
    void exhaustDrawsAndCountersMerfolk() {
        Permanent mindspringMerfolk = addCreatureReady(player1, new MindspringMerfolk());
        Permanent coralMerfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new CoralMerfolk());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        addExhaustMana(2);

        harness.activateAbility(player1, 0, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(mindspringMerfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(coralMerfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player2, "Coral Merfolk").getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each exhaust ability can be activated only once")
    void cannotExhaustTwice() {
        addCreatureReady(player1, new MindspringMerfolk());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        addExhaustMana(2);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();

        addExhaustMana(2);
        Permanent mindspringMerfolk = findPermanents(player1, "Mindspring Merfolk").getFirst();
        mindspringMerfolk.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("X zero still taps the source and puts counters on Merfolk")
    void zeroXStillCountersMerfolk() {
        Permanent merfolk = addCreatureReady(player1, new MindspringMerfolk());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addExhaustMana(0);

        harness.activateAbility(player1, 0, 0, 0, null);

        assertThat(merfolk.isTapped()).isTrue();
        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Merfolk entering after activation receive counters at resolution")
    void countersUseBattlefieldAtResolution() {
        Permanent source = addCreatureReady(player1, new MindspringMerfolk());
        addExhaustMana(0);

        harness.activateAbility(player1, 0, 0, 0, null);
        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new MindspringMerfolk());
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Separate Mindspring Merfolk each have their own exhaust activation")
    void exhaustLimitIsPerPermanent() {
        Permanent first = addCreatureReady(player1, new MindspringMerfolk());
        Permanent second = addCreatureReady(player1, new MindspringMerfolk());
        addExhaustMana(0);

        harness.activateAbility(player1, 0, 0, 0, null);
        harness.passBothPriorities();
        addExhaustMana(0);
        harness.activateAbility(player1, 1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the exhaust tap cost")
    void summoningSicknessPreventsExhaust() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new MindspringMerfolk());
        merfolk.setSummoningSick(true);
        addExhaustMana(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(merfolk.isTapped()).isFalse();
        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void addExhaustMana(int x) {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, x);
    }

}
