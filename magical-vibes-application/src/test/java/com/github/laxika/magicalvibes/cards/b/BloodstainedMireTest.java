package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.s.SmolderingMarsh;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({BloodstainedMire.class, GlorySeeker.class, Mountain.class, Plains.class, Swamp.class, SmolderingMarsh.class})
class BloodstainedMireTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Bloodstained Mire pays 1 life, sacrifices it, and searches for a Swamp or Mountain")
    void activationPaysLifeSacrificesAndSearchesForMatchingLand() {
        activateSearch();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Bloodstained Mire");
        harness.assertInGraveyard(player1, "Bloodstained Mire");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.getName().equals("Swamp") || card.getName().equals("Mountain"))
                .noneMatch(card -> card.getName().equals("Plains") || card.getName().equals("Glory Seeker"));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Search resolves without a choice when the library has no Swamp or Mountain")
    void searchWithNoMatchingCards() {
        activateSearchWithLibrary(List.of(new Plains(), new GlorySeeker()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bloodstained Mire");
        harness.assertInGraveyard(player1, "Bloodstained Mire");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Search ability cannot be activated while Bloodstained Mire is tapped")
    void searchRequiresUntappedSource() {
        Permanent mire = harness.addToBattlefieldAndReturn(player1, new BloodstainedMire());
        mire.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(mire);
    }

    @Test
    @DisplayName("Chosen Swamp or Mountain enters the battlefield untapped")
    void chosenLandEntersUntapped() {
        activateSearch();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> (permanent.getCard().getName().equals("Swamp")
                        || permanent.getCard().getName().equals("Mountain")) && !permanent.isTapped());
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
    @DisplayName("A library containing only a Mountain can supply the searched land")
    void findsMountainWithoutSwamp() {
        Mountain mountain = new Mountain();
        activateSearchWithLibrary(List.of(mountain));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(mountain);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A library containing only a Swamp can supply the searched land")
    void findsSwampWithoutMountain() {
        Swamp swamp = new Swamp();
        activateSearchWithLibrary(List.of(swamp));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Swamp");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(swamp);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A nonbasic Swamp Mountain is eligible and obeys its own enters-tapped ability")
    void findsNonbasicLandAndAppliesEntryReplacement() {
        SmolderingMarsh marsh = new SmolderingMarsh();
        activateSearchWithLibrary(List.of(marsh));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Smoldering Marsh");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(marsh);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library does not prevent paying the activation costs")
    void emptyLibraryStillPaysCostsAndResolves() {
        activateSearchWithLibrary(List.of());

        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player1, "Bloodstained Mire");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An unpaid life cost leaves the source untapped and unsacrificed")
    void cannotActivateWithoutLifeToPay() {
        Permanent mire = harness.addToBattlefieldAndReturn(player1, new BloodstainedMire());
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(mire);
        assertThat(mire.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Bloodstained Mire");
        assertThat(gd.stack).isEmpty();
    }

    private void activateSearch() {
        activateSearchWithLibrary(List.of(new Swamp(), new Mountain(), new Plains(), new GlorySeeker()));
    }

    private void activateSearchWithLibrary(List<Card> library) {
        harness.addToBattlefield(player1, new BloodstainedMire());
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, null, null);
    }
}
