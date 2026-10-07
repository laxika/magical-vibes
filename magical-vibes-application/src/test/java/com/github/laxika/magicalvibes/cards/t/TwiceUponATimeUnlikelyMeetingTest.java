package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.u.UnlikelyMeeting;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwiceUponATimeUnlikelyMeeting.class, UnlikelyMeeting.class, TheFourthDoctor.class,
        TheTenthDoctor.class, Island.class})
class TwiceUponATimeUnlikelyMeetingTest extends BaseCardTest {

    @Test
    void cannotCastTwiceUponATimeWithoutTwoDoctors() {
        harness.addToBattlefield(player1, new TheFourthDoctor());
        harness.setHand(player1, List.of(new TwiceUponATimeUnlikelyMeeting()));
        addFrontFaceMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void twiceUponATimeTakesAnExtraTurnAndExilesItself() {
        harness.addToBattlefield(player1, new TheFourthDoctor());
        harness.addToBattlefield(player1, new TheTenthDoctor());
        TwiceUponATimeUnlikelyMeeting card = new TwiceUponATimeUnlikelyMeeting();
        harness.setHand(player1, List.of(card));
        addFrontFaceMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Twice Upon a Time");
    }

    @Test
    void unlikelyMeetingSearchesForADoctorAndExilesTheAdventureCard() {
        TheFourthDoctor doctor = new TheFourthDoctor();
        harness.setLibrary(player1, List.of(doctor, new Island()));
        TwiceUponATimeUnlikelyMeeting card = new TwiceUponATimeUnlikelyMeeting();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getId).containsExactly(doctor.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(doctor.getId());
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void opponentsDoctorsDoNotSatisfyTheCastingRestriction() {
        harness.addToBattlefield(player1, new TheFourthDoctor());
        harness.addToBattlefield(player2, new TheTenthDoctor());
        harness.setHand(player1, List.of(new TwiceUponATimeUnlikelyMeeting()));
        addFrontFaceMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void losingDoctorsAfterCastingDoesNotPreventResolution() {
        harness.addToBattlefield(player1, new TheFourthDoctor());
        harness.addToBattlefield(player1, new TheTenthDoctor());
        TwiceUponATimeUnlikelyMeeting card = new TwiceUponATimeUnlikelyMeeting();
        harness.setHand(player1, List.of(card));
        addFrontFaceMana();

        harness.castSorcery(player1, 0, 0);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void unlikelyMeetingCanFailToFindEvenWhenADoctorIsAvailable() {
        TheFourthDoctor doctor = new TheFourthDoctor();
        harness.setLibrary(player1, List.of(doctor, new Island()));
        TwiceUponATimeUnlikelyMeeting card = new TwiceUponATimeUnlikelyMeeting();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId).contains(doctor.getId());
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void cannotCastTwiceUponATimeFromAdventureExileWithoutTwoDoctors() {
        TwiceUponATimeUnlikelyMeeting card = resolveAdventureWithNoDoctorsInLibrary();
        addFrontFaceMana();

        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void canCastTwiceUponATimeFromAdventureExileWithTwoDoctorsButCannotCastItAgain() {
        TwiceUponATimeUnlikelyMeeting card = resolveAdventureWithNoDoctorsInLibrary();
        harness.addToBattlefield(player1, new TheFourthDoctor());
        harness.addToBattlefield(player1, new TheTenthDoctor());
        addFrontFaceMana();

        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Twice Upon a Time");
        addFrontFaceMana();
        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private TwiceUponATimeUnlikelyMeeting resolveAdventureWithNoDoctorsInLibrary() {
        harness.setLibrary(player1, List.of(new Island()));
        TwiceUponATimeUnlikelyMeeting card = new TwiceUponATimeUnlikelyMeeting();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.extraTurns).isEmpty();
        return card;
    }

    private void addFrontFaceMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
