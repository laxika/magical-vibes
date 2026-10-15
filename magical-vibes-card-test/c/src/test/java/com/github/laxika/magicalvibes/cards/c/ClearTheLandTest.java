package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GerrardsIrregulars;
import com.github.laxika.magicalvibes.cards.h.HickoryWoodlot;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RishadanPort;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        ClearTheLand.class,
        Forest.class,
        GerrardsIrregulars.class,
        HickoryWoodlot.class,
        Island.class,
        Mountain.class,
        Plains.class,
        RishadanPort.class,
        Swamp.class
})
class ClearTheLandTest extends BaseCardTest {

    @Test
    @DisplayName("Each player puts revealed lands onto the battlefield tapped and exiles the rest")
    void eachPlayerPutsLandsOntoBattlefieldAndExilesRest() {
        Card forest = new Forest();
        Card creature = new GerrardsIrregulars();
        Card island = new Island();
        Card otherCreature = new GerrardsIrregulars();
        Card mountain = new Mountain();
        harness.setLibrary(player1, List.of(forest, creature, island, otherCreature, mountain));

        Card swamp = new Swamp();
        Card plains = new Plains();
        Card opponentCreature = new GerrardsIrregulars();
        Card opponentOtherCreature = new GerrardsIrregulars();
        Card opponentForest = new Forest();
        harness.setLibrary(player2,
                List.of(swamp, plains, opponentCreature, opponentOtherCreature, opponentForest));

        harness.castFromHand(player1, new ClearTheLand(), "{2}{G}");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Forest", "Island", "Mountain");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Swamp", "Plains", "Forest");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allMatch(permanent -> permanent.isTapped());

        assertThat(gd.exiledCards)
                .extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(creature, otherCreature, opponentCreature, opponentOtherCreature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the top five cards are processed, including nonbasic lands")
    void processesOnlyTopFiveIncludingNonbasicLands() {
        Card nonbasicLand = new RishadanPort();
        Card creature = new GerrardsIrregulars();
        Card otherCreature = new GerrardsIrregulars();
        Card thirdCreature = new GerrardsIrregulars();
        Card fourthCreature = new GerrardsIrregulars();
        Card cardBeyondTopFive = new Forest();
        harness.setLibrary(player1, List.of(
                nonbasicLand, creature, otherCreature, thirdCreature, fourthCreature, cardBeyondTopFive));
        harness.setLibrary(player2, List.of());

        harness.castFromHand(player1, new ClearTheLand(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Rishadan Port").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(cardBeyondTopFive);
        assertThat(gd.exiledCards)
                .extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(creature, otherCreature, thirdCreature, fourthCreature);
    }

    @Test
    @DisplayName("A library with fewer than five cards reveals all available cards")
    void libraryShorterThanFive() {
        Card forest = new Forest();
        Card creature = new GerrardsIrregulars();
        harness.setLibrary(player1, List.of(forest, creature));
        harness.setLibrary(player2, List.of());

        harness.castFromHand(player1, new ClearTheLand(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Revealed depletion lands enter tapped with their normal counters")
    void revealedLandsRetainTheirEntryCounters() {
        harness.setLibrary(player1, List.of(new HickoryWoodlot(), new Forest()));
        harness.setLibrary(player2, List.of(new HickoryWoodlot()));

        harness.castFromHand(player1, new ClearTheLand(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Hickory Woodlot").getCounterCount(CounterType.DEPLETION))
                .isEqualTo(2);
        assertThat(findPermanent(player2, "Hickory Woodlot").getCounterCount(CounterType.DEPLETION))
                .isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Nonland-only libraries exile their revealed cards without affecting existing permanents")
    void nonlandOnlyLibrariesExileAllRevealedCards() {
        Card creature = new GerrardsIrregulars();
        Card opponentCreature = new GerrardsIrregulars();
        harness.setLibrary(player1, List.of(creature));
        harness.setLibrary(player2, List.of(opponentCreature));
        var existingForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.castFromHand(player1, new ClearTheLand(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(creature, opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(existingForest);
        assertThat(existingForest.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Empty libraries do not cause a player to lose because no cards are drawn")
    void bothLibrariesEmpty() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        harness.castFromHand(player1, new ClearTheLand(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Clear the Land");
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }
}
