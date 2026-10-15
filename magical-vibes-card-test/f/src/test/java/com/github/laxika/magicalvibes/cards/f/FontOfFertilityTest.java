package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TempleOfMalady;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FontOfFertility.class, Plains.class, Forest.class, Island.class, GrizzlyBears.class,
        TempleOfMalady.class})
class FontOfFertilityTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Font of Fertility sacrifices it and puts the ability on the stack")
    void activatingSacrificesSelf() {
        addFontAndMana();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Font of Fertility");
        harness.assertInGraveyard(player1, "Font of Fertility");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving presents only basic lands for the battlefield-tapped search")
    void resolvingPresentsBasicLands() {
        addFontAndMana();
        setupLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(3)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Choosing a basic land puts it onto the battlefield tapped")
    void chosenBasicLandEntersTapped() {
        addFontAndMana();
        setupLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A basic land search can fail to find even when a basic land is available")
    void canDeclineToFindBasicLand() {
        addFontAndMana();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Font of Fertility");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A library containing no basic lands finishes the search without moving a card")
    void noBasicLandsFinishesSearch() {
        addFontAndMana();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not prevent activating and resolving Font of Fertility")
    void emptyLibraryFinishesSearch() {
        addFontAndMana();
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Font of Fertility");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The selected land leaves only its controller's library and enters under their control")
    void selectedLandMovesFromControllersLibrary() {
        addFontAndMana();
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        Island opponentsLand = new Island();
        harness.setLibrary(player1, List.of(forest, bears));
        harness.setLibrary(player2, List.of(opponentsLand));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Nonbasic lands are excluded from the search")
    void excludesNonbasicLand() {
        addFontAndMana();
        Forest forest = new Forest();
        TempleOfMalady temple = new TempleOfMalady();
        harness.setLibrary(player1, List.of(temple, forest));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(temple);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addFontAndMana() {
        harness.addToBattlefield(player1, new FontOfFertility());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
