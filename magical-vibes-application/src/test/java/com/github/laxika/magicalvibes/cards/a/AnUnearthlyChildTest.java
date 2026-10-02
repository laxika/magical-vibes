package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RoseTyler;
import com.github.laxika.magicalvibes.cards.t.Tardis;
import com.github.laxika.magicalvibes.cards.t.TheFirstDoctor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnUnearthlyChild.class, Island.class, RoseTyler.class, Tardis.class, TheFirstDoctor.class})
class AnUnearthlyChildTest extends BaseCardTest {

    @Test
    void chapterPutsTheFirstDoctorIntoHandAndBottomsTheRest() {
        Card nonmatch = card(CardType.CREATURE);
        Card doctor = card(CardType.CREATURE);
        doctor.setSubtypes(List.of(CardSubtype.DOCTOR));
        gd.playerHands.get(player1.getId()).clear();
        harness.setLibrary(player1, List.of(doctor, nonmatch));
        addSagaWithLore(0);

        resolveNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(doctor);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatch);
    }

    @Test
    void chaptersFindCardsWithDoctorsCompanionAndVehicles() {
        Card companion = card(CardType.CREATURE);
        companion.setKeywords(Set.of(Keyword.DOCTORS_COMPANION));
        Card vehicle = card(CardType.ARTIFACT);
        vehicle.setSubtypes(List.of(CardSubtype.VEHICLE));

        gd.playerHands.get(player1.getId()).clear();
        Permanent saga = addSagaWithLore(0);
        harness.setLibrary(player1, List.of(companion, card(CardType.CREATURE)));
        resolveNextChapter();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(companion);

        saga.setCounterCount(CounterType.LORE, 1);
        harness.setLibrary(player1, List.of(vehicle, card(CardType.CREATURE)));
        resolveNextChapter();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(companion, vehicle);
    }

    @Test
    void enteringTriggersChapterOneAndStopsAtTheFirstMatch() {
        Island first = new Island();
        Island second = new Island();
        TheFirstDoctor doctor = new TheFirstDoctor();
        RoseTyler companion = new RoseTyler();
        Tardis vehicle = new Tardis();
        harness.setLibrary(player1, List.of(first, second, doctor, companion, vehicle));
        harness.setLibrary(player2, List.of(new Island()));

        harness.castFromHand(player1, new AnUnearthlyChild(), "{1}{U}{U}");
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(doctor);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(companion, vehicle);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 4))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void finalChapterFindsVehicleBeforeSagaIsSacrificed() {
        Tardis vehicle = new Tardis();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(vehicle));
        Permanent saga = addSagaWithLore(2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(vehicle);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    @Test
    void secondChapterFindsRealDoctorsCompanion() {
        RoseTyler companion = new RoseTyler();
        Island nonmatch = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(nonmatch, companion));
        addSagaWithLore(1);

        resolveNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(companion);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatch);
    }

    @Test
    void noMatchReturnsEntireLibraryWithoutPuttingAnythingIntoHand() {
        Island first = new Island();
        Island second = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        addSagaWithLore(0);

        resolveNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void emptyLibraryDoesNotDrawOrLoseTheGame() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        Permanent saga = addSagaWithLore(0);

        resolveNextChapter();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new AnUnearthlyChild());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void resolveNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Card card(CardType type) {
        Card card = new Card();
        card.setType(type);
        return card;
    }
}
