package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrosanTusker.class, Forest.class, GrizzlyBears.class})
class KrosanTuskerTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling may reveal a basic land into hand before drawing")
    void cyclingSearchesBasicLandThenDraws() {
        harness.setHand(player1, List.of(new KrosanTusker()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Krosan Tusker");
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling search offers only basic lands to hand")
    void searchFiltersToBasicLands() {
        harness.setHand(player1, List.of(new KrosanTusker()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(1);
        assertThat(search.params().cards().getFirst().hasType(CardType.LAND)).isTrue();
        assertThat(search.params().cards().getFirst().getSupertypes()).contains(CardSupertype.BASIC);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
    }

    @Test
    @DisplayName("Declining the search still draws a card")
    void decliningSearchStillDraws() {
        harness.setHand(player1, List.of(new KrosanTusker()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Krosan Tusker");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
