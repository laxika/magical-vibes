package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forest.class, GrizzlyBears.class, Island.class, MythosOfBrokkos.class, Shock.class})
class MythosOfBrokkosTest extends BaseCardTest {

    @Test
    void returnsUpToTwoPermanentCardsWithoutEnhancedEffect() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card thirdPermanent = new Island();
        Card instant = new Shock();
        Card libraryCard = new Island();
        harness.setGraveyard(player1, List.of(creature, land, thirdPermanent, instant));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new MythosOfBrokkos()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.GraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.mandatory()).isFalse();
        assertThat(choice.validIndices()).containsExactly(0, 1, 2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);

        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void enhancedEffectSearchesThenReturnsPermanentCards() {
        Card existingPermanent = new Forest();
        Card instant = new Shock();
        Card searchedCard = new Island();
        harness.setGraveyard(player1, List.of(existingPermanent, instant));
        harness.setLibrary(player1, List.of(searchedCard));
        harness.setHand(player1, List.of(new MythosOfBrokkos()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void mayReturnZeroPermanentCards() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(new MythosOfBrokkos()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayStopAfterReturningOnePermanentCard() {
        Card forest = new Forest();
        Card island = new Island();
        harness.setGraveyard(player1, List.of(forest, island));
        harness.setHand(player1, List.of(new MythosOfBrokkos()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island).doesNotContain(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLUE", "BLACK"})
    void spendingOnlyOneEnhancedColorDoesNotSearch(ManaColor color) {
        Card forest = new Forest();
        Card libraryCard = new Island();
        harness.setGraveyard(player1, List.of(forest));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new MythosOfBrokkos()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, color, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void enhancedSearchCanFindAnInstantButCannotReturnIt() {
        Card forest = new Forest();
        Card instant = new Shock();
        harness.setGraveyard(player1, List.of(forest));
        harness.setLibrary(player1, List.of(instant));
        harness.setHand(player1, List.of(new MythosOfBrokkos()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.GraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void enhancedSpellWithEmptyLibraryStillReturnsAPermanent() {
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new MythosOfBrokkos()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
