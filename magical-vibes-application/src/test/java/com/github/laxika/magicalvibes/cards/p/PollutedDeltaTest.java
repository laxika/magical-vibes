package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BarrenMoor;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LonelySandbar;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PollutedDelta.class, BarrenMoor.class, GlorySeeker.class, Island.class, LonelySandbar.class,
        Mountain.class, Swamp.class})
class PollutedDeltaTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Polluted Delta pays 1 life, sacrifices it, and searches for an Island or Swamp")
    void activationPaysLifeSacrificesAndSearchesForMatchingLand() {
        activateSearch();

        harness.assertLife(player1, 19);
        harness.assertNotOnBattlefield(player1, "Polluted Delta");
        harness.assertInGraveyard(player1, "Polluted Delta");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.getName().equals("Island") || card.getName().equals("Swamp"))
                .containsExactlyInAnyOrderElementsOf(List.of(
                        gd.playerDecks.get(player1.getId()).get(0),
                        gd.playerDecks.get(player1.getId()).get(1)))
                .noneMatch(card -> card.getName().equals("Mountain")
                        || card.getName().equals("Barren Moor")
                        || card.getName().equals("Glory Seeker")
                        || card.getName().equals("Lonely Sandbar"));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Chosen Island or Swamp enters the battlefield untapped")
    void chosenLandEntersUntapped() {
        activateSearch();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> (permanent.getCard().getName().equals("Island")
                        || permanent.getCard().getName().equals("Swamp")) && !permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find")
    void canFailToFind() {
        activateSearch();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Search ability cannot be activated while Polluted Delta is tapped")
    void searchRequiresUntappedSource() {
        Permanent delta = harness.addToBattlefieldAndReturn(player1, new PollutedDelta());
        delta.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(delta);
    }

    @Test
    @DisplayName("Search resolves without a choice when the library has no Island or Swamp")
    void searchWithNoMatchingCards() {
        harness.addToBattlefield(player1, new PollutedDelta());
        harness.setLibrary(player1, List.of(new Mountain(), new BarrenMoor(), new LonelySandbar(), new GlorySeeker()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Polluted Delta");
        harness.assertInGraveyard(player1, "Polluted Delta");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new PollutedDelta());
        harness.setLibrary(player1, List.of(new Island(), new Swamp(), new Mountain(), new BarrenMoor(),
                new LonelySandbar(), new GlorySeeker()));

        harness.activateAbility(player1, 0, null, null);
    }
}
