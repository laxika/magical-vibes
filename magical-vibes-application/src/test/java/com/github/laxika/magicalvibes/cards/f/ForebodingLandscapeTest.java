package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForebodingLandscape.class, Swamp.class, Forest.class, Island.class, Plains.class, GrizzlyBears.class})
class ForebodingLandscapeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana produces one colorless")
    void tappingProducesColorlessMana() {
        harness.addToBattlefield(player1, new ForebodingLandscape());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Search ability sacrifices the land and presents only basic Swamps, Forests, and Islands")
    void searchPresentsMatchingBasicLands() {
        harness.addToBattlefield(player1, new ForebodingLandscape());
        setupLibrary();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Foreboding Landscape");
        harness.assertInGraveyard(player1, "Foreboding Landscape");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.getName().equals("Swamp")
                        || card.getName().equals("Forest")
                        || card.getName().equals("Island"))
                .noneMatch(card -> card.getName().equals("Plains") || card.getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Cycling pays black, green, and blue, discards the card, and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ForebodingLandscape()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Foreboding Landscape");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("Each permitted basic land enters tapped and only the chosen card leaves the library")
    void chosenBasicLandEntersTapped(int choice) {
        harness.addToBattlefield(player1, new ForebodingLandscape());
        var swamp = new Swamp();
        var forest = new Forest();
        var island = new Island();
        var nonbasic = new ForebodingLandscape();
        var basics = List.of(swamp, forest, island);
        harness.setLibrary(player1, List.of(swamp, forest, island, nonbasic));

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Foreboding Landscape");
        harness.assertNotOnBattlefield(player1, "Foreboding Landscape");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(swamp, forest, island);
        harness.handleCardChosen(player1, choice);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard()).isSameAs(basics.get(choice));
            assertThat(permanent.isTapped()).isTrue();
        });
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3)
                .contains(nonbasic).doesNotContain(basics.get(choice));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A restricted search may fail to find even with a matching land available")
    void canDeclineToFind() {
        harness.addToBattlefield(player1, new ForebodingLandscape());
        var swamp = new Swamp();
        harness.setLibrary(player1, List.of(swamp));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(swamp);
        harness.assertInGraveyard(player1, "Foreboding Landscape");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching a library without a matching land completes without a prompt")
    void noMatchingLandCompletesSearch() {
        harness.addToBattlefield(player1, new ForebodingLandscape());
        var plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for mana prevents activating the tap and sacrifice ability")
    void cannotSearchAfterTappingForMana() {
        harness.addToBattlefield(player1, new ForebodingLandscape());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Foreboding Landscape");
        assertThat(findPermanent(player1, "Foreboding Landscape").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling pays and discards immediately but draws only on resolution")
    void cyclingCostsArePaidBeforeDrawing() {
        var landscape = new ForebodingLandscape();
        var island = new Island();
        harness.setHand(player1, List.of(landscape));
        harness.setLibrary(player1, List.of(island));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Foreboding Landscape");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.stack).hasSize(1);
        for (var color : List.of(ManaColor.BLACK, ManaColor.GREEN, ManaColor.BLUE)) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLACK", "GREEN", "BLUE"})
    @DisplayName("Cycling cannot replace any required colored mana with colorless mana")
    void cyclingRequiresEachColor(ManaColor missingColor) {
        var landscape = new ForebodingLandscape();
        harness.setHand(player1, List.of(landscape));
        for (var color : List.of(ManaColor.BLACK, ManaColor.GREEN, ManaColor.BLUE)) {
            if (color != missingColor) {
                harness.addMana(player1, color, 1);
            }
        }
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(landscape);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Swamp(), new Forest(), new Island(), new Plains(), new GrizzlyBears()));
    }
}
