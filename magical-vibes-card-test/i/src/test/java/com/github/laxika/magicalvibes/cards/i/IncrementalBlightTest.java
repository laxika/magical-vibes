package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MossbridgeTroll;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IncrementalBlight.class, SerraAngel.class, GrizzlyBears.class, MossbridgeTroll.class,
        Forest.class})
class IncrementalBlightTest extends BaseCardTest {

    private void addMana(int amount) {
        harness.addMana(player1, ManaColor.BLACK, amount);
    }

    @Test
    @DisplayName("Places 1, 2 and 3 -1/-1 counters on the three targets respectively")
    void placesCountersOnEachTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new IncrementalBlight()));
        addMana(5);

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId(), third.getId()));

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(third.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enough -1/-1 counters destroy a creature")
    void countersDestroyCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IncrementalBlight()));
        addMana(5);

        // 2 counters on the Serra Angel (survives), 3 counters on the 2/2 Grizzly Bears (dies).
        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), angel.getId(), bear.getId()));

        harness.assertOnBattlefield(player2, "Serra Angel");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target the same creature more than once")
    void cannotTargetSameCreatureTwice() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new IncrementalBlight()));
        addMana(5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new MossbridgeTroll());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new MossbridgeTroll());
        harness.setHand(player1, List.of(new IncrementalBlight()));
        addMana(5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(forest.getId(), firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Still affects legal targets when one target leaves before resolution")
    void resolvesRemainingLegalTargetsWhenOneLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MossbridgeTroll());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MossbridgeTroll());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new MossbridgeTroll());
        harness.setHand(player1, List.of(new IncrementalBlight()));
        addMana(5);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId(), third.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot cast with fewer than three targets")
    void requiresThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MossbridgeTroll());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MossbridgeTroll());
        harness.setHand(player1, List.of(new IncrementalBlight()));
        addMana(5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 targets");
    }

    @Test
    @DisplayName("The third target still gets three counters when the first two leave")
    void preservesThirdTargetCounterCountWhenOtherTargetsLeave() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MossbridgeTroll());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MossbridgeTroll());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new MossbridgeTroll());
        harness.setHand(player1, List.of(new IncrementalBlight()));
        addMana(5);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId(), third.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerBattlefields.get(player2.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(third.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Incremental Blight");
    }

    @Test
    @DisplayName("Does not resolve when all three targets leave")
    void doesNotResolveWhenAllTargetsLeave() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MossbridgeTroll());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MossbridgeTroll());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new MossbridgeTroll());
        harness.setHand(player1, List.of(new IncrementalBlight()));
        addMana(5);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId(), third.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(second, third));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(third.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Incremental Blight");
    }
}
