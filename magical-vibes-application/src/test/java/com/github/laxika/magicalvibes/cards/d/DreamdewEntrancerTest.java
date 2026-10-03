package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamdewEntrancer.class})
class DreamdewEntrancerTest extends BaseCardTest {

    @Test
    void tapsAndPutsThreeStunCountersOnOpponentCreatureWithoutDrawing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DreamdewEntrancer());
        harness.setLibrary(player1, List.of(new DreamdewEntrancer(), new DreamdewEntrancer()));
        castDreamdewEntrancer(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsTwoCardsWhenTargetingCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DreamdewEntrancer());
        harness.setLibrary(player1, List.of(new DreamdewEntrancer(), new DreamdewEntrancer()));
        castDreamdewEntrancer(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void mayChooseNoTarget() {
        harness.setLibrary(player1, List.of(new DreamdewEntrancer(), new DreamdewEntrancer()));

        harness.castFromHand(player1, new DreamdewEntrancer(), "{2}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Dreamdew Entrancer");
    }

    @Test
    void addsStunCountersAndDrawsEvenWhenOwnTargetIsAlreadyTapped() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DreamdewEntrancer());
        target.setTapped(true);
        target.setCounterCount(CounterType.STUN, 1);
        harness.setLibrary(player1, List.of(new DreamdewEntrancer(), new DreamdewEntrancer()));

        castDreamdewEntrancer(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void canTargetItselfAndDrawTwoCards() {
        harness.setLibrary(player1, List.of(new DreamdewEntrancer(), new DreamdewEntrancer()));
        harness.castFromHand(player1, new DreamdewEntrancer(), "{2}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent entrant = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, entrant.getId());
        harness.passBothPriorities();

        assertThat(entrant.isTapped()).isTrue();
        assertThat(entrant.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotDrawWhenControlOfOwnTargetIsLostBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DreamdewEntrancer());
        harness.setLibrary(player1, List.of(new DreamdewEntrancer(), new DreamdewEntrancer()));
        harness.castFromHand(player1, new DreamdewEntrancer(), "{2}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsWhenOpponentTargetComesUnderYourControlBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DreamdewEntrancer());
        harness.setLibrary(player1, List.of(new DreamdewEntrancer(), new DreamdewEntrancer()));
        harness.castFromHand(player1, new DreamdewEntrancer(), "{2}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotDrawWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DreamdewEntrancer());
        harness.setLibrary(player1, List.of(new DreamdewEntrancer(), new DreamdewEntrancer()));
        harness.castFromHand(player1, new DreamdewEntrancer(), "{2}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerHands.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
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
