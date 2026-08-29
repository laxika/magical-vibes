package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamdewEntrancer.class, GrizzlyBears.class})
class DreamdewEntrancerTest extends BaseCardTest {

    @Test
    void tapsAndPutsThreeStunCountersOnOpponentCreatureWithoutDrawing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        castDreamdewEntrancer(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsTwoCardsWhenTargetingCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        castDreamdewEntrancer(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void mayChooseNoTarget() {
        harness.setHand(player1, List.of(new DreamdewEntrancer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Dreamdew Entrancer");
    }

    private void castDreamdewEntrancer(Permanent target) {
        harness.setHand(player1, List.of(new DreamdewEntrancer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
