package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DaggerbackBasilisk;
import com.github.laxika.magicalvibes.cards.r.Regress;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtGorger.class, DaggerbackBasilisk.class, Regress.class, Solemnity.class})
class ThoughtGorgerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a counter for each hand card and then discards the hand")
    void entersWithCountersAndDiscardsHand() {
        harness.setHand(player1, List.of(
                new ThoughtGorger(), new DaggerbackBasilisk(), new DaggerbackBasilisk(), new DaggerbackBasilisk()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent gorger = findPermanent(player1, "Thought Gorger");
        assertThat(gorger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Leaves the battlefield and draws one card for each +1/+1 counter")
    void leavingDrawsForEachPlusOneCounter() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new DaggerbackBasilisk(), new DaggerbackBasilisk(), new DaggerbackBasilisk()));
        Permanent gorger = harness.addToBattlefieldAndReturn(player1, new ThoughtGorger());
        gorger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, gorger));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Enters normally with an empty hand")
    void entersWithAnEmptyHand() {
        harness.setHand(player1, List.of(new ThoughtGorger()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Thought Gorger")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returning to hand draws for all +1/+1 counters and ignores other counters")
    void returningToHandDrawsUsingLastKnownPlusOneCounters() {
        Permanent gorger = harness.addToBattlefieldAndReturn(player1, new ThoughtGorger());
        gorger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gorger.setCounterCount(CounterType.CHARGE, 3);
        harness.setHand(player1, List.of(new Regress()));
        harness.setLibrary(player1, List.of(
                new DaggerbackBasilisk(), new DaggerbackBasilisk(), new DaggerbackBasilisk()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, gorger.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thought Gorger");
        harness.assertInHand(player1, "Thought Gorger");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not discard when Solemnity prohibits placing counters")
    @CardUsed(Solemnity.class)
    void doesNotDiscardWhenCountersCannotBePlaced() {
        harness.addToBattlefield(player2, new Solemnity());
        harness.setHand(player1, List.of(
                new ThoughtGorger(), new DaggerbackBasilisk(), new DaggerbackBasilisk()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Thought Gorger")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not discard if it leaves before its enter-the-battlefield ability resolves")
    void doesNotDiscardIfItLeavesBeforeEnterTriggerResolves() {
        List<Card> hand = List.of(
                new ThoughtGorger(), new DaggerbackBasilisk(), new DaggerbackBasilisk(), new DaggerbackBasilisk());
        harness.setHand(player1, hand);
        harness.setLibrary(player1, List.of(new DaggerbackBasilisk(), new DaggerbackBasilisk()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent gorger = findPermanent(player1, "Thought Gorger");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, gorger));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }
}
