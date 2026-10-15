package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.v.VampireHexmage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RewriteHistory.class, LlanowarElves.class, GrizzlyBears.class, HolyDay.class, Divination.class, VampireHexmage.class})
class RewriteHistoryTest extends BaseCardTest {

    @Test
    @DisplayName("Loots and puts a plan counter on itself when a creature becomes tapped")
    void lootsAndAddsPlanCounter() {
        Permanent rewriteHistory = harness.addToBattlefieldAndReturn(player1, new RewriteHistory());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        Card discarded = new GrizzlyBears();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elves));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(rewriteHistory.getCounterCount(CounterType.PLAN)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Sacrifices itself at four plan counters and returns up to two instants or sorceries")
    void sacrificesAtFourCountersAndReturnsSpells() {
        Permanent rewriteHistory = harness.addToBattlefieldAndReturn(player1, new RewriteHistory());
        rewriteHistory.setCounterCount(CounterType.PLAN, 3);
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        Card instant = new HolyDay();
        Card sorcery = new Divination();
        Card nonSpell = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(instant, sorcery, nonSpell));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elves));
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rewriteHistory);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(instant, sorcery);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(instant.getId(), sorcery.getId());

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId(), sorcery.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .doesNotContain(rewriteHistory.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rewriteHistory.getCard(), nonSpell);
        assertThat(gd.playerHands.get(player1.getId())).contains(instant, sorcery);
    }

    @Test
    @DisplayName("Simultaneously tapping two attackers loots only once")
    void simultaneousAttackersLootOnce() {
        Permanent rewriteHistory = harness.addToBattlefieldAndReturn(player1, new RewriteHistory());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Card discarded = new GrizzlyBears();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn, new GrizzlyBears()));

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(rewriteHistory.getCounterCount(CounterType.PLAN)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("An opponent's creature becoming tapped does not loot")
    void opponentCreatureDoesNotTrigger() {
        Permanent rewriteHistory = harness.addToBattlefieldAndReturn(player1, new RewriteHistory());
        Permanent elves = addCreatureReady(player2, new LlanowarElves());
        Card handCard = new GrizzlyBears();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);

        harness.tapPermanent(player2, gd.playerBattlefields.get(player2.getId()).indexOf(elves));
        resolveAllTriggers();

        assertThat(rewriteHistory.getCounterCount(CounterType.PLAN)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The spell discarded for the fourth counter can be returned after the sacrifice")
    void returnsSpellDiscardedForFourthCounter() {
        Permanent rewriteHistory = harness.addToBattlefieldAndReturn(player1, new RewriteHistory());
        rewriteHistory.setCounterCount(CounterType.PLAN, 3);
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        Card discarded = new HolyDay();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elves));
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rewriteHistory);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(discarded.getId());
        harness.handleMultipleCardsChosen(player1, List.of(discarded.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discarded);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rewriteHistory.getCard());
    }

    @Test
    @CardUsed({VampireHexmage.class})
    @DisplayName("Removing plan counters in response does not stop the fourth-counter sacrifice")
    void sacrificeStillResolvesAfterCountersRemoved() {
        Permanent rewriteHistory = harness.addToBattlefieldAndReturn(player1, new RewriteHistory());
        rewriteHistory.setCounterCount(CounterType.PLAN, 3);
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        Permanent hexmage = addCreatureReady(player1, new VampireHexmage());
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elves));
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        PendingInteraction.MultiGraveyardChoice prematureChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        if (prematureChoice != null) {
            harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        }

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hexmage),
                null, rewriteHistory.getId());
        harness.passBothPriorities();
        assertThat(rewriteHistory.getCounterCount(CounterType.PLAN)).isZero();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rewriteHistory);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rewriteHistory.getCard());
    }

    @Test
    @DisplayName("Separate creature taps each loot and add a plan counter")
    void separateTapsLootTwice() {
        Permanent rewriteHistory = harness.addToBattlefieldAndReturn(player1, new RewriteHistory());
        Permanent first = addCreatureReady(player1, new LlanowarElves());
        Permanent second = addCreatureReady(player1, new LlanowarElves());
        Card original = new GrizzlyBears();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setHand(player1, List.of(original));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(first));
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(second));
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        assertThat(rewriteHistory.getCounterCount(CounterType.PLAN)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(original, firstDraw);
    }

    @Test
    @DisplayName("Choosing no cards still sacrifices Rewrite History")
    void canChooseNoCards() {
        Permanent rewriteHistory = harness.addToBattlefieldAndReturn(player1, new RewriteHistory());
        rewriteHistory.setCounterCount(CounterType.PLAN, 3);
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elves));
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rewriteHistory);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant, rewriteHistory.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(instant);
    }
}
