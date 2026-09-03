package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CrystalVein;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BadRiver.class, CrystalVein.class, Forest.class, Island.class, Mountain.class, Swamp.class})
class BadRiverTest extends BaseCardTest {

    @Test
    @DisplayName("Bad River enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new BadRiver()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent river = findPermanent(player1, "Bad River");
        assertThat(river.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Search ability cannot be activated while Bad River is tapped")
    void searchRequiresUntappedSource() {
        Permanent river = harness.addToBattlefieldAndReturn(player1, new BadRiver());
        river.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(river);
    }

    @Test
    @DisplayName("Search ability sacrifices Bad River and presents only Island or Swamp cards")
    void searchPresentsOnlyIslandOrSwamp() {
        activateSearch();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bad River");
        harness.assertInGraveyard(player1, "Bad River");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Island") || c.getName().equals("Swamp"))
                .anyMatch(c -> c.getName().equals("Island"))
                .anyMatch(c -> c.getName().equals("Swamp"))
                .noneMatch(c -> c.getName().equals("Forest") || c.getName().equals("Mountain"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Search resolves without a choice when the library has no Island or Swamp")
    void searchWithNoMatchingCards() {
        harness.addToBattlefield(player1, new BadRiver());
        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new CrystalVein()));
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bad River");
        harness.assertInGraveyard(player1, "Bad River");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chosen land enters the battlefield untapped")
    void chosenLandEntersUntapped() {
        activateSearch();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Island") && !p.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find")
    void canFailToFind() {
        activateSearch();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new BadRiver());
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Island(), new Swamp(), new Forest(), new Mountain(), new CrystalVein()));
    }
}
