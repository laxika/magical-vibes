package com.github.laxika.magicalvibes.cards.a;

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

@CardUsed(AnUnearthlyChild.class)
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
