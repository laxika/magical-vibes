package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.h.HardenedScales;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({CommonBond.class, HillGiant.class, FountainOfYouth.class, HardenedScales.class, Unsummon.class})
class CommonBondTest extends BaseCardTest {

    @Test
    @DisplayName("Puts both +1/+1 counters on a single target creature")
    void putsBothCountersOnOneTarget() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new CommonBond()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, List.of(giant.getId()));

        // Hill Giant (3/3) with two +1/+1 counters → 5/5.
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(giant.getEffectivePower()).isEqualTo(5);
        assertThat(giant.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Distributes one +1/+1 counter on each of two target creatures")
    void distributesOneCounterEachAmongTwoTargets() {
        Permanent giant1 = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent giant2 = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new CommonBond()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, List.of(giant1.getId(), giant2.getId()));

        // Each Hill Giant (3/3) with one +1/+1 counter → 4/4.
        assertThat(giant1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(giant1.getEffectivePower()).isEqualTo(4);
        assertThat(giant2.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(giant2.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new CommonBond()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Hardened Scales applies separately to each counter instruction on the same creature")
    void appliesReplacementToEachCounterInstruction() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player1, new HardenedScales());
        harness.setHand(player1, List.of(new CommonBond()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, List.of(giant.getId()));

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("A remaining legal target receives only its own counter")
    void doesNotRedistributeCounterFromAnIllegalTarget(boolean returnFirstTarget) {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent returned = returnFirstTarget ? first : second;
        Permanent remaining = returnFirstTarget ? second : first;
        harness.setHand(player1, List.of(new CommonBond()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.castAndResolveInstant(player2, 0, returned.getId());
        harness.passBothPriorities();

        assertThat(remaining.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both counters can be put on an opponent's creature")
    void canPutBothCountersOnOpponentsCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new CommonBond()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, List.of(giant.getId()));

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
