package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreamscapeArtist.class, Forest.class, Island.class, GrizzlyBears.class})
class DreamscapeArtistTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card, sacrifices a land, and puts up to two basic lands onto the battlefield")
    void searchesForUpToTwoBasicLands() {
        Permanent artist = addCreatureReady(player1, new DreamscapeArtist());
        Forest sacrificedLand = new Forest();
        harness.addToBattlefield(player1, sacrificedLand);
        GrizzlyBears discarded = new GrizzlyBears();
        Forest forest = new Forest();
        Island island = new Island();
        GrizzlyBears nonBasic = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(forest, island, nonBasic));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(artist.isTapped()).isTrue();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(artist.getCard(), forest, island);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonBasic);
    }

    @Test
    @DisplayName("Cannot activate without a land to sacrifice")
    void requiresLandToSacrifice() {
        addCreatureReady(player1, new DreamscapeArtist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May find no basic lands")
    void mayFindNoBasicLands() {
        Permanent artist = addCreatureReady(player1, new DreamscapeArtist());
        Forest sacrificedLand = new Forest();
        harness.addToBattlefield(player1, sacrificedLand);
        GrizzlyBears discarded = new GrizzlyBears();
        Forest forest = new Forest();
        GrizzlyBears nonBasic = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(forest, nonBasic));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(artist.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactly(artist.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, nonBasic);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Puts one basic land onto the battlefield when only one is available")
    void searchesForOnlyOneAvailableBasicLand() {
        Permanent artist = addCreatureReady(player1, new DreamscapeArtist());
        Forest sacrificedLand = new Forest();
        harness.addToBattlefield(player1, sacrificedLand);
        GrizzlyBears discarded = new GrizzlyBears();
        Forest forest = new Forest();
        GrizzlyBears nonBasic = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(forest, nonBasic));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(artist.getCard(), forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonBasic);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void requiresCardToDiscard() {
        addCreatureReady(player1, new DreamscapeArtist());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The selected lands enter together after the search choices are complete")
    void selectedLandsEnterSimultaneouslyAndUntapped() {
        Permanent artist = addCreatureReady(player1, new DreamscapeArtist());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactly(artist.getCard());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(artist.getCard(), forest, island);
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Island").isTapped()).isFalse();
    }

    @Test
    @DisplayName("May stop after finding one basic land even when another is available")
    void mayFindOnlyOneOfTwoAvailableBasicLands() {
        Permanent artist = addCreatureReady(player1, new DreamscapeArtist());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(artist.getCard(), forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLUE, 3);
    }
}
