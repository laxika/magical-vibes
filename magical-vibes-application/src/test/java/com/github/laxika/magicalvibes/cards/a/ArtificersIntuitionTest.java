package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.e.EnsouledScimitar;
import com.github.laxika.magicalvibes.cards.s.SparkElemental;
import com.github.laxika.magicalvibes.cards.w.WayfarersBauble;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArtificersIntuition.class, ConjurersBauble.class, EnsouledScimitar.class,
        SparkElemental.class, WayfarersBauble.class})
class ArtificersIntuitionTest extends BaseCardTest {

    @Test
    @DisplayName("Discards an artifact and searches for an artifact with mana value 1 or less")
    void discardsArtifactAndSearchesForCheapArtifact() {
        Card artifactToDiscard = new ConjurersBauble();
        Card cheapArtifact = new WayfarersBauble();
        Card expensiveArtifact = new EnsouledScimitar();
        Card nonArtifactInHand = new SparkElemental();
        Card nonArtifactInLibrary = new SparkElemental();
        harness.addToBattlefield(player1, new ArtificersIntuition());
        harness.setHand(player1, List.of(nonArtifactInHand, artifactToDiscard));
        harness.setLibrary(player1, List.of(expensiveArtifact, nonArtifactInLibrary, cheapArtifact));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.DiscardCostChoice discardChoice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardCostChoice.class);
        assertThat(discardChoice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(cheapArtifact);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifactToDiscard);
        assertThat(gd.playerHands.get(player1.getId())).contains(cheapArtifact);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(expensiveArtifact, nonArtifactInLibrary);
    }

    @Test
    @DisplayName("May activate with no matching card in the library")
    void mayActivateWithNoMatchingCardInLibrary() {
        Card artifactToDiscard = new ConjurersBauble();
        Card expensiveArtifact = new EnsouledScimitar();
        Card nonArtifact = new SparkElemental();
        harness.addToBattlefield(player1, new ArtificersIntuition());
        harness.setHand(player1, List.of(artifactToDiscard));
        harness.setLibrary(player1, List.of(expensiveArtifact, nonArtifact));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifactToDiscard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(expensiveArtifact, nonArtifact);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot activate without an artifact card to discard")
    void cannotActivateWithoutArtifactToDiscard() {
        harness.addToBattlefield(player1, new ArtificersIntuition());
        harness.setHand(player1, List.of(new SparkElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without blue mana")
    void cannotActivateWithoutBlueMana() {
        harness.addToBattlefield(player1, new ArtificersIntuition());
        harness.setHand(player1, List.of(new ConjurersBauble()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
