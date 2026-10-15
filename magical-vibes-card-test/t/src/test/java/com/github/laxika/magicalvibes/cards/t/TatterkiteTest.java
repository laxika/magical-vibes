package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FateTransfer;
import com.github.laxika.magicalvibes.cards.l.LeechBonder;
import com.github.laxika.magicalvibes.cards.s.ScarscaleRitual;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Tatterkite.class, Skinrender.class, LeechBonder.class, FateTransfer.class, ScarscaleRitual.class})
class TatterkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Tatterkite can't have -1/-1 counters put on it by Skinrender ETB")
    void cantHaveMinusOneMinusOneCountersFromSkinrender() {
        Permanent tatterkite = harness.addToBattlefieldAndReturn(player1, new Tatterkite());
        harness.setHand(player2, List.of(new Skinrender()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0, tatterkite.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(tatterkite.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(tatterkite.getEffectivePower()).isEqualTo(2);
        assertThat(tatterkite.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Query service reports Tatterkite can't have counters")
    void cantHaveCountersReported() {
        Permanent tatterkite = harness.addToBattlefieldAndReturn(player1, new Tatterkite());
        assertThat(gqs.cantHaveCounters(gd, tatterkite)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = CounterType.class, names = {"PLUS_ONE_PLUS_ONE", "MINUS_ONE_MINUS_ONE", "CHARGE"})
    @DisplayName("Counters remain on their source when Fate Transfer targets Tatterkite")
    void countersCannotBeMovedOntoTatterkite(CounterType counterType) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LeechBonder());
        Permanent tatterkite = harness.addToBattlefieldAndReturn(player2, new Tatterkite());
        source.setCounterCount(counterType, 2);
        harness.setHand(player1, List.of(new FateTransfer()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), tatterkite.getId()));

        assertThat(source.getCounterCount(counterType)).isEqualTo(2);
        assertThat(tatterkite.getCounterCount(counterType)).isZero();
    }

    @Test
    @DisplayName("Tatterkite cannot pay Scarscale Ritual's counter placement cost")
    void cannotPayCounterPlacementCost() {
        Permanent tatterkite = harness.addToBattlefieldAndReturn(player1, new Tatterkite());
        harness.setHand(player1, List.of(new ScarscaleRitual()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, tatterkite.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tatterkite.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
