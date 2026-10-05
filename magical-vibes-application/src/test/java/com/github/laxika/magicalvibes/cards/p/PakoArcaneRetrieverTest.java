package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HaldanAvidArcanist;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
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

@CardUsed({PakoArcaneRetriever.class, GiantGrowth.class, GrizzlyBears.class,
        HaldanAvidArcanist.class, YavimayaCoast.class})
class PakoArcaneRetrieverTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Haldan")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card haldan = new HaldanAvidArcanist();
        harness.setLibrary(player2, List.of(haldan));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new PakoArcaneRetriever());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(haldan);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking exiles each top card with fetch counters and grows for noncreatures")
    void attackingExilesTopCardsAndCountsNoncreatures() {
        Card ownTopCard = new GiantGrowth();
        Card opposingTopCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opposingTopCard));
        Permanent pako = addCreatureReady(player1, new PakoArcaneRetriever());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownTopCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opposingTopCard);
        assertThat(gd.exiledCardsWithFetchCounters)
                .contains(ownTopCard.getId(), opposingTopCard.getId());
        assertThat(pako.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, pako)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, pako)).isEqualTo(4);
    }

    @Test
    @DisplayName("The target player may decline the partner search")
    void partnerSearchMayBeDeclined() {
        Card haldan = new HaldanAvidArcanist();
        harness.setLibrary(player2, List.of(haldan));
        harness.setHand(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new PakoArcaneRetriever());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(haldan);
    }

    @Test
    @DisplayName("Lands count as noncreatures and an empty library is skipped")
    void attackingWithOneEmptyLibraryCountsLand() {
        Card land = new YavimayaCoast();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(land));
        Permanent pako = addCreatureReady(player1, new PakoArcaneRetriever());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(land);
        assertThat(gd.exiledCardsWithFetchCounters).contains(land.getId());
        assertThat(pako.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two noncreatures give two counters and only the top cards are exiled")
    void attackingCountsBothNoncreatures() {
        Card first = new YavimayaCoast();
        Card second = new YavimayaCoast();
        Card lower = new HaldanAvidArcanist();
        harness.setLibrary(player1, List.of(first, lower));
        harness.setLibrary(player2, List.of(second));
        Permanent pako = addCreatureReady(player1, new PakoArcaneRetriever());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(lower);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCardsWithFetchCounters).contains(first.getId(), second.getId());
        assertThat(pako.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiling only creatures gives no counters")
    void attackingCountsNoCreatures() {
        Card first = new HaldanAvidArcanist();
        Card second = new HaldanAvidArcanist();
        harness.setLibrary(player1, List.of(first));
        harness.setLibrary(player2, List.of(second));
        Permanent pako = addCreatureReady(player1, new PakoArcaneRetriever());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.exiledCardsWithFetchCounters).contains(first.getId(), second.getId());
        assertThat(pako.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The attack trigger still exiles cards after Pako leaves")
    void attackTriggerResolvesWithoutPako() {
        Card land = new YavimayaCoast();
        harness.setLibrary(player1, List.of(land));
        harness.setLibrary(player2, List.of());
        Permanent pako = addCreatureReady(player1, new PakoArcaneRetriever());

        declareAttackers(List.of(0));
        assertThat(gd.stack).isNotEmpty();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, pako));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);
        assertThat(gd.exiledCardsWithFetchCounters).contains(land.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(pako.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
