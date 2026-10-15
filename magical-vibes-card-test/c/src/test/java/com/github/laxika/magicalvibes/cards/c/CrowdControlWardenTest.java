package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({CrowdControlWarden.class, GrizzlyBears.class, Shock.class})
class CrowdControlWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a counter for each other creature its controller controls")
    void entersWithCountersForOtherControlledCreatures() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        harness.castFromHand(player1, new CrowdControlWarden(), "{3}{G}{W}");
        harness.passBothPriorities();

        Permanent warden = findPermanent(player1, "Crowd-Control Warden");
        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Turns face up with a counter for each other creature its controller controls")
    void turnsFaceUpWithCountersForOtherControlledCreatures() {
        harness.setHand(player1, List.of(new CrowdControlWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        Permanent warden = findPermanent(player1, "Crowd-Control Warden");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(warden));

        assertThat(warden.isFaceDown()).isFalse();
        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void entersWithoutCountersWhenThereAreNoOtherControlledCreatures() {
        harness.castFromHand(player1, new CrowdControlWarden(), "{3}{G}{W}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Crowd-Control Warden")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"GREEN", "WHITE"})
    void faceDownEntryHasNoCountersAndEitherHybridColorCanPayToTurnFaceUp(ManaColor color) {
        addCreatureReady(player1, new CrowdControlWarden());
        addCreatureReady(player2, new CrowdControlWarden());
        harness.setHand(player1, List.of(new CrowdControlWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent warden = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown).findFirst().orElseThrow();
        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, color, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(warden));

        assertThat(warden.isFaceDown()).isFalse();
        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void turningFaceUpWithoutPayingStillAddsCountersImmediately() {
        harness.setHand(player1, List.of(new CrowdControlWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent warden = findPermanent(player1, "Crowd-Control Warden");
        addCreatureReady(player1, new CrowdControlWarden());
        addCreatureReady(player1, new CrowdControlWarden());
        addCreatureReady(player2, new CrowdControlWarden());

        harness.inMutationScope(() -> gs.turnPermanentFaceUpWithoutPayingManaCost(gd, warden));

        assertThat(warden.isFaceDown()).isFalse();
        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void disguiseWardTriggersWhenOpponentTargetsFaceDownWarden() {
        harness.setHand(player1, List.of(new CrowdControlWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent warden = findPermanent(player1, "Crowd-Control Warden");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, warden.getId());

        assertThat(gd.stack).hasSize(2);

    }
}
