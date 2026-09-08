package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({MigrationPath.class, Plains.class, Forest.class, GrizzlyBears.class})
class MigrationPathTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for up to two basic lands and puts them onto the battlefield tapped")
    void searchesForBasicLandsTapped() {
        castMigrationPath(List.of(new Plains(), new Forest(), new GrizzlyBears()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanent(player1, "Plains").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Migration Path");
    }

    @Test
    @DisplayName("Cycling discards Migration Path and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new MigrationPath()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Migration Path");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void castMigrationPath(List<Card> library) {
        harness.setHand(player1, List.of(new MigrationPath()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.setLibrary(player1, library);
    }
}
