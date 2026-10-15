package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.m.MerrowCommerce;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.r.RiverDarter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JadeBearer.class, RiverDarter.class, RaptorCompanion.class, MerrowCommerce.class})
class JadeBearerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on another Merfolk you control")
    void etbPutsCounterOnAnotherMerfolk() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new RiverDarter());
        harness.setHand(player1, List.of(new JadeBearer()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, List.of(merfolk.getId()));
        resolveAllTriggers();

        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-Merfolk creature you control")
    void cannotTargetNonMerfolk() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion()).getId();
        harness.setHand(player1, List.of(new JadeBearer()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another Merfolk creature you control");
    }

    @Test
    @DisplayName("Cannot target an opponent's Merfolk")
    void cannotTargetOpponentMerfolk() {
        UUID merfolkId = harness.addToBattlefieldAndReturn(player2, new RiverDarter()).getId();
        harness.setHand(player1, List.of(new JadeBearer()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(merfolkId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another Merfolk creature you control");
    }

    @Test
    @DisplayName("Can enter without an ETB target when no other Merfolk is available")
    void canEnterWithoutTarget() {
        harness.setHand(player1, List.of(new JadeBearer()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB can put a counter on a noncreature Merfolk permanent")
    void canTargetNoncreatureMerfolk() {
        Permanent commerce = harness.addToBattlefieldAndReturn(player1, new MerrowCommerce());
        harness.setHand(player1, List.of(new JadeBearer()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, List.of(commerce.getId()));
        resolveAllTriggers();

        assertThat(commerce.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Another Jade Bearer is a legal target, but the entering one receives no counter")
    void canTargetAnotherJadeBearer() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new JadeBearer());
        harness.setHand(player1, List.of(new JadeBearer()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, List.of(other.getId()));
        resolveAllTriggers();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> !permanent.getId().equals(other.getId()))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    @DisplayName("ETB does not put counters on a target that left the battlefield")
    void removedTargetReceivesNoCounter() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new RiverDarter());
        harness.setHand(player1, List.of(new JadeBearer()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, List.of(merfolk.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(merfolk);
        resolveAllTriggers();

        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
