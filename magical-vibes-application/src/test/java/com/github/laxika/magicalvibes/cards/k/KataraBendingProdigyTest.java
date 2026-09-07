package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KataraBendingProdigy.class, GrizzlyBears.class})
class KataraBendingProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter at your end step while tapped")
    void getsCounterAtEndStepWhileTapped() {
        Permanent katara = harness.addToBattlefieldAndReturn(player1, new KataraBendingProdigy());
        katara.tap();

        resolveEndStep();

        assertThat(katara.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a +1/+1 counter at your end step while untapped")
    void doesNotGetCounterAtEndStepWhileUntapped() {
        Permanent katara = harness.addToBattlefieldAndReturn(player1, new KataraBendingProdigy());

        resolveEndStep();

        assertThat(katara.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Waterbend taps six artifacts or creatures and draws a card")
    void waterbendTapsSixPermanentsAndDraws() {
        Permanent katara = harness.addToBattlefieldAndReturn(player1, new KataraBendingProdigy());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent thirdCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fourthCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fifthCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(katara.isTapped()).isTrue();
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(thirdCreature.isTapped()).isTrue();
        assertThat(fourthCreature.isTapped()).isTrue();
        assertThat(fifthCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Waterbend cannot be paid without six available payments")
    void waterbendRequiresSixPayments() {
        Permanent katara = harness.addToBattlefieldAndReturn(player1, new KataraBendingProdigy());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("waterbend");

        assertThat(katara.isTapped()).isFalse();
        assertThat(firstCreature.isTapped()).isFalse();
    }

    private void resolveEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
