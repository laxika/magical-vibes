package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MoonCircuitHacker;
import com.github.laxika.magicalvibes.cards.n.NetworkDisruptor;
import com.github.laxika.magicalvibes.cards.v.VectorGlider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheModernAge.class, VectorGlider.class, MoonCircuitHacker.class, NetworkDisruptor.class})
class TheModernAgeTest extends BaseCardTest {

    @Test
    void chapterIDrawsThenDiscards() {
        harness.setHand(player1, List.of(new NetworkDisruptor()));
        harness.setLibrary(player1, List.of(new MoonCircuitHacker()));
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Moon-Circuit Hacker");
        harness.assertInGraveyard(player1, "Network Disruptor");
    }

    @Test
    void chapterIIDrawsThenDiscards() {
        harness.setHand(player1, List.of(new NetworkDisruptor()));
        harness.setLibrary(player1, List.of(new MoonCircuitHacker()));
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Moon-Circuit Hacker");
        harness.assertInGraveyard(player1, "Network Disruptor");
    }

    @Test
    void chapterIIITransformsIntoVectorGlider() {
        Permanent saga = addSagaWithLore(2);
        saga.tap();

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent glider = findPermanent(player1, "Vector Glider");
        assertThat(glider).isNotNull();
        assertThat(glider.isTransformed()).isTrue();
        assertThat(glider.getId()).isNotEqualTo(saga.getId());
        assertThat(glider.getCounterCount(CounterType.LORE)).isZero();
        assertThat(glider.isTapped()).isFalse();
        assertThat(glider.isSummoningSick()).isTrue();
        harness.assertNotOnBattlefield(player1, "The Modern Age");
        harness.assertNotInGraveyard(player1, "The Modern Age");
    }

    @Test
    void chapterICanDiscardTheNewlyDrawnCard() {
        harness.setHand(player1, List.of(new NetworkDisruptor()));
        harness.setLibrary(player1, List.of(new MoonCircuitHacker()));
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Network Disruptor");
        harness.assertNotInHand(player1, "Moon-Circuit Hacker");
        harness.assertInGraveyard(player1, "Moon-Circuit Hacker");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void enteringSagaTriggersChapterIImmediately() {
        harness.setLibrary(player1, List.of(new MoonCircuitHacker()));
        harness.castFromHand(player1, new TheModernAge(), "{1}{U}");
        harness.passBothPriorities();

        Permanent saga = findPermanent(player1, "The Modern Age");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }

        harness.assertInGraveyard(player1, "Moon-Circuit Hacker");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "The Modern Age");
    }

    @Test
    void chapterIIICannotReturnSagaThatLeftBeforeResolution() {
        Permanent saga = addSagaWithLore(2);
        advanceToNextChapter();
        gd.playerBattlefields.get(player1.getId()).remove(saga);
        gd.playerGraveyards.get(player1.getId()).add(saga.getOriginalCard());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vector Glider");
        harness.assertInGraveyard(player1, "The Modern Age");
    }

    @Test
    void chapterIIIReturnsUnderAbilityControllersControlRatherThanOwners() {
        TheModernAge card = new TheModernAge();
        card.setOwnerId(player2.getId());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, card);
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vector Glider");
        harness.assertNotOnBattlefield(player2, "Vector Glider");
        assertThat(findPermanent(player1, "Vector Glider").getOriginalCard().getOwnerId())
                .isEqualTo(player2.getId());
    }

    @Test
    void chapterIStillDrawsAndDiscardsAfterSagaLeavesBattlefield() {
        harness.setHand(player1, List.of(new NetworkDisruptor()));
        harness.setLibrary(player1, List.of(new MoonCircuitHacker()));
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        gd.playerBattlefields.get(player1.getId()).remove(saga);
        gd.playerGraveyards.get(player1.getId()).add(saga.getOriginalCard());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Moon-Circuit Hacker");
        harness.assertInGraveyard(player1, "Network Disruptor");
        harness.assertInGraveyard(player1, "The Modern Age");
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheModernAge());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }
}
