package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FlightSpellbomb;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsochronScepter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArtificersIntuitionTest extends BaseCardTest {

    @Test
    @DisplayName("Discards an artifact and searches for an artifact with mana value 1 or less")
    void discardsArtifactAndSearchesForCheapArtifact() {
        Card artifactToDiscard = new FlightSpellbomb();
        Card cheapArtifact = new Spellbook();
        Card expensiveArtifact = new IsochronScepter();
        Card creature = new GoldMyr();
        harness.addToBattlefield(player1, new ArtificersIntuition());
        harness.setHand(player1, List.of(new GrizzlyBears(), artifactToDiscard));
        harness.setLibrary(player1, List.of(expensiveArtifact, creature, cheapArtifact));
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

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifactToDiscard);
        assertThat(gd.playerHands.get(player1.getId())).contains(cheapArtifact);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(expensiveArtifact, creature);
    }

    @Test
    @DisplayName("Cannot activate without an artifact card to discard")
    void cannotActivateWithoutArtifactToDiscard() {
        harness.addToBattlefield(player1, new ArtificersIntuition());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
