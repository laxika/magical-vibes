package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeepwoodDenizen.class, GrizzlyBears.class, Forest.class})
class DeepwoodDenizenTest extends BaseCardTest {

    @Test
    @DisplayName("Draw ability costs one less for each +1/+1 counter on creatures you control")
    void drawAbilityCostsLessForControlledCreatureCounters() {
        Permanent denizen = addCreatureReady(player1, new DeepwoodDenizen());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(denizen.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 8})
    void ownCountersReduceOnlyGenericMana(int counters) {
        Permanent denizen = addCreatureReady(player1, new DeepwoodDenizen());
        denizen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, Math.max(0, 5 - counters));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(denizen.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void sumsCountersAcrossControlledCreatures() {
        Permanent denizen = addCreatureReady(player1, new DeepwoodDenizen());
        Permanent other = addCreatureReady(player1, new DeepwoodDenizen());
        denizen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void ignoresOpposingCreaturesNoncreaturesAndOtherCounterTypes() {
        Permanent denizen = addCreatureReady(player1, new DeepwoodDenizen());
        denizen.setCounterCount(CounterType.CHARGE, 5);
        Permanent opponent = addCreatureReady(player2, new DeepwoodDenizen());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(denizen.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void excessCountersCannotPayGreenManaRequirement() {
        Permanent denizen = addCreatureReady(player1, new DeepwoodDenizen());
        denizen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(denizen.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void cannotActivateWhileTappedOrSummoningSick(boolean summoningSick) {
        Permanent denizen = addCreatureReady(player1, new DeepwoodDenizen());
        denizen.setTapped(!summoningSick);
        denizen.setSummoningSick(summoningSick);
        denizen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void counterChangesAfterActivationDoNotChangePaidCostOrPreventDrawing() {
        Permanent denizen = addCreatureReady(player1, new DeepwoodDenizen());
        denizen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        denizen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void vigilanceAllowsAttackingWithoutTapping() {
        Permanent denizen = addCreatureReady(player1, new DeepwoodDenizen());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(denizen.isAttacking()).isTrue();
        assertThat(denizen.isTapped()).isFalse();
    }
}
