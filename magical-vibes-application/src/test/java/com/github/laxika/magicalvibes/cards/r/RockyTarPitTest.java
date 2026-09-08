package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RockyTarPit.class, CrystalVein.class, Forest.class, Island.class, Mountain.class, Swamp.class})
class RockyTarPitTest extends BaseCardTest {

    @Test
    @DisplayName("Rocky Tar Pit enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new RockyTarPit()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent pit = findPermanent(player1, "Rocky Tar Pit");
        assertThat(pit.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Search ability cannot be activated while Rocky Tar Pit is tapped")
    void searchRequiresUntappedSource() {
        Permanent pit = harness.addToBattlefieldAndReturn(player1, new RockyTarPit());
        pit.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(pit);
    }

    @Test
    @DisplayName("Search ability sacrifices Rocky Tar Pit and presents only Swamp or Mountain cards")
    void searchPresentsOnlySwampOrMountain() {
        activateSearch();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rocky Tar Pit");
        harness.assertInGraveyard(player1, "Rocky Tar Pit");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Swamp") || c.getName().equals("Mountain"))
                .anyMatch(c -> c.getName().equals("Swamp"))
                .anyMatch(c -> c.getName().equals("Mountain"))
                .noneMatch(c -> c.getName().equals("Forest") || c.getName().equals("Island"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Chosen land enters the battlefield untapped")
    void chosenLandEntersUntapped() {
        activateSearch();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Swamp") && !p.isTapped());
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

    @Test
    @DisplayName("Search resolves without a choice when the library has no Swamp or Mountain")
    void searchWithNoMatchingCards() {
        harness.addToBattlefield(player1, new RockyTarPit());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new CrystalVein()));
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rocky Tar Pit");
        harness.assertInGraveyard(player1, "Rocky Tar Pit");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new RockyTarPit());
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Swamp(), new Mountain(), new Forest(), new Island(), new CrystalVein()));
    }
}
