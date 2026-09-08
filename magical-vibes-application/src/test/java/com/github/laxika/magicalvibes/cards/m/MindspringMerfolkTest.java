package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Mindspring Merfolk")
class MindspringMerfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Exhaust draws X and puts counters on each Merfolk creature you control")
    void exhaustDrawsAndCountersMerfolk() {
        Permanent mindspringMerfolk = addMindspringMerfolk();
        Permanent coralMerfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new CoralMerfolk());
        setLibrary(new Forest(), new Forest());
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
        addMindspringMerfolk();
        setLibrary(new Forest(), new Forest(), new Forest(), new Forest());
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

    private Permanent addMindspringMerfolk() {
        Permanent mindspringMerfolk = harness.addToBattlefieldAndReturn(player1, new MindspringMerfolk());
        mindspringMerfolk.setSummoningSick(false);
        return mindspringMerfolk;
    }

    private void addExhaustMana(int x) {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, x);
    }

    private void setLibrary(Forest... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
