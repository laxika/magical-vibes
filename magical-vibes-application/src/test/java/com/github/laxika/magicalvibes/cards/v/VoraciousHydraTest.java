package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VoraciousHydraTest extends BaseCardTest {

    @Test
    void doubleCountersModeDoublesTheCountersItEnteredWith() {
        castHydra(0, 3, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findHydra().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void fightModeFightsTheChosenCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castHydra(1, 3, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findHydra().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void fightModeCannotTargetACreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> castHydra(1, 3, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castHydra(int modeIndex, int xValue, UUID targetId) {
        harness.setHand(player1, List.of(new VoraciousHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        gs.playModalXCard(gd, player1, 0, modeIndex, xValue, targetId);
    }

    private Permanent findHydra() {
        return findPermanent(player1, "Voracious Hydra");
    }
}
