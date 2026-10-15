package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.ScuteSwarm;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoilingRegrowth.class, Mountain.class, Plains.class, Forest.class, ScuteSwarm.class})
class RoilingRegrowthTest extends BaseCardTest {

    @Test
    @DisplayName("The land is sacrificed when Roiling Regrowth resolves")
    void sacrificesLandOnResolution() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        castRoilingRegrowth();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Resolving offers up to two basic lands for the tapped battlefield")
    void offersBasicLandsTapped() {
        castRoilingRegrowth();
        List<Card> library = setUpLibrary();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrderElementsOf(library.subList(0, 2));
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Both selected basic lands enter tapped")
    void selectedBasicLandsEnterTapped() {
        castRoilingRegrowth();
        setUpLibrary();

        harness.passBothPriorities();

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .filteredOn(Permanent::isTapped)
                .hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The player may fail to find basic lands")
    void canFailToFind() {
        castRoilingRegrowth();
        setUpLibrary();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Plains")
                        || permanent.getCard().getName().equals("Forest"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The player may find only one of two available basic lands")
    void canFindOnlyOneLand() {
        castRoilingRegrowth();
        setUpLibrary();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Plains"))
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Scute Swarm");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Both basic lands enter together after the search choices are complete")
    void landsEnterTogether() {
        castRoilingRegrowth();
        setUpLibrary();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only the chosen land is sacrificed before the search resumes")
    void choosesOneControlledLandToSacrifice() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        castRoilingRegrowth();
        setUpLibrary();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mountain, forest);
        harness.handleMultiplePermanentsChosen(player1, List.of(mountain.getId()));

        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest).doesNotContain(mountain);
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A land is still sacrificed when the library is empty")
    void sacrificesLandWithEmptyLibrary() {
        harness.addToBattlefield(player1, new Mountain());
        castRoilingRegrowth();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Roiling Regrowth");
        harness.assertNotOnBattlefield(player1, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castRoilingRegrowth() {
        harness.setHand(player1, List.of(new RoilingRegrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0);
    }

    private List<Card> setUpLibrary() {
        Card plains = new Plains();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(plains, forest, new ScuteSwarm()));
        return List.of(plains, forest);
    }
}
