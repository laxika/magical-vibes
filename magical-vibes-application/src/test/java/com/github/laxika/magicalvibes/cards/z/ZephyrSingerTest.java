package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.FinalFlourish;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorDrinker;
import com.github.laxika.magicalvibes.cards.s.StrionicResonator;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZephyrSinger.class, GrizzlyBears.class, IchorDrinker.class, FinalFlourish.class, StrionicResonator.class})
class ZephyrSingerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a flying counter on each creature that convoked it")
    void putsFlyingCountersOnConvokeCreatures() {
        Permanent firstConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent nonConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ZephyrSinger()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(firstConvokeCreature.getId(), secondConvokeCreature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstConvokeCreature.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(secondConvokeCreature.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(firstConvokeCreature.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(nonConvokeCreature.getCounterCount(CounterType.FLYING)).isZero();
    }

    @Test
    @DisplayName("Casting without convoke puts no flying counters on other creatures")
    void castingWithoutConvokePutsNoCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        harness.castFromHand(player1, new ZephyrSinger(), "{2}{U}{U}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zephyr Singer");
        assertThat(creature.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Summoning-sick creatures can pay the entire cost with convoke")
    void paysEntireCostWithConvoke() {
        Permanent firstBlue = harness.addToBattlefieldAndReturn(player1, new ZephyrSinger());
        Permanent secondBlue = harness.addToBattlefieldAndReturn(player1, new ZephyrSinger());
        Permanent firstGeneric = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        Permanent secondGeneric = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        List<Permanent> creatures = List.of(firstBlue, secondBlue, firstGeneric, secondGeneric);
        creatures.forEach(creature -> creature.setSummoningSick(true));
        harness.setHand(player1, List.of(new ZephyrSinger()));

        harness.castInstantWithConvoke(player1, 0, List.of(),
                creatures.stream().map(Permanent::getId).toList());

        creatures.forEach(creature -> {
            assertThat(creature.isTapped()).isTrue();
            assertThat(creature.getCounterCount(CounterType.FLYING)).isZero();
        });
        harness.passBothPriorities();
        creatures.forEach(creature ->
                assertThat(creature.getCounterCount(CounterType.FLYING)).isZero());
        harness.passBothPriorities();

        creatures.forEach(creature -> {
            assertThat(creature.getCounterCount(CounterType.FLYING)).isEqualTo(1);
            assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();
        });
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("A convoking creature leaving before the trigger resolves does not prevent other counters")
    void skipsConvokeCreatureThatLeftBattlefield() {
        Permanent leaving = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        harness.setHand(player1, List.of(new ZephyrSinger()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new FinalFlourish()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(leaving.getId(), remaining.getId()));
        harness.passBothPriorities();
        harness.castInstant(player2, 0, leaving.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(leaving);
        harness.passBothPriorities();

        assertThat(remaining.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(leaving.getCounterCount(CounterType.FLYING)).isZero();
    }

    @Test
    @DisplayName("Cannot tap more creatures for convoke than the total mana cost")
    void rejectsExcessConvokeCreatures() {
        List<Permanent> creatures = List.of(
                harness.addToBattlefieldAndReturn(player1, new ZephyrSinger()),
                harness.addToBattlefieldAndReturn(player1, new ZephyrSinger()),
                harness.addToBattlefieldAndReturn(player1, new IchorDrinker()),
                harness.addToBattlefieldAndReturn(player1, new IchorDrinker()),
                harness.addToBattlefieldAndReturn(player1, new IchorDrinker()));
        harness.setHand(player1, List.of(new ZephyrSinger()));

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                creatures.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A copied enters ability puts another flying counter on the same convoking creatures")
    void copiedEntersAbilityRemembersConvokeCreatures() {
        harness.addToBattlefield(player1, new StrionicResonator());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        harness.setHand(player1, List.of(new ZephyrSinger()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, gd.stack.getLast().getTargetableId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.FLYING)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.FLYING)).isEqualTo(2);
        assertThat(other.getCounterCount(CounterType.FLYING)).isZero();
    }
}
