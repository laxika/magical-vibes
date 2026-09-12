package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MarshBoa;
import com.github.laxika.magicalvibes.cards.v.VintaraElephant;
import com.github.laxika.magicalvibes.cards.w.WintermoonMesa;
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

@CardUsed({Thrive.class, MarshBoa.class, VintaraElephant.class, WintermoonMesa.class})
class ThriveTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one +1/+1 counter on each of X target creatures")
    void putsCountersOnEachTarget() {
        Permanent boa = harness.addToBattlefieldAndReturn(player2, new MarshBoa());
        Permanent elephant = harness.addToBattlefieldAndReturn(player2, new VintaraElephant());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player2, new MarshBoa());
        harness.setHand(player1, List.of(new Thrive()));
        harness.addMana(player1, ManaColor.GREEN, 3); // X=2: {2}{G}

        harness.castSorcery(player1, 0, 2, List.of(boa.getId(), elephant.getId()));
        harness.passBothPriorities();

        assertThat(boa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(elephant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(untargeted.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Requires exactly X targets")
    void requiresExactlyXTargets() {
        Permanent boa = harness.addToBattlefieldAndReturn(player2, new MarshBoa());
        harness.setHand(player1, List.of(new Thrive()));
        harness.addMana(player1, ManaColor.GREEN, 3); // X=2

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(boa.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects more than X targets")
    void rejectsMoreThanXTargets() {
        Permanent boa = harness.addToBattlefieldAndReturn(player2, new MarshBoa());
        Permanent elephant = harness.addToBattlefieldAndReturn(player2, new VintaraElephant());
        harness.setHand(player1, List.of(new Thrive()));
        harness.addMana(player1, ManaColor.GREEN, 2); // X=1: {1}{G}

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, 1, List.of(boa.getId(), elephant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires different targets")
    void requiresDifferentTargets() {
        Permanent boa = harness.addToBattlefieldAndReturn(player2, new MarshBoa());
        harness.setHand(player1, List.of(new Thrive()));
        harness.addMana(player1, ManaColor.GREEN, 3); // X=2: {2}{G}

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, 2, List.of(boa.getId(), boa.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=0 resolves without targets")
    void xZeroResolvesWithoutTargets() {
        Permanent boa = harness.addToBattlefieldAndReturn(player2, new MarshBoa());
        harness.setHand(player1, List.of(new Thrive()));
        harness.addMana(player1, ManaColor.GREEN, 1); // X=0: {0}{G}

        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(boa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new WintermoonMesa());
        harness.setHand(player1, List.of(new Thrive()));
        harness.addMana(player1, ManaColor.GREEN, 2); // X=1: {1}{G}

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
