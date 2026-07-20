package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShefetMonitorTest extends BaseCardTest {

    private void setupAndCycle(List<Card> library) {
        harness.setHand(player1, List.of(new ShefetMonitor()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities(); // resolve cycling ability -> may-search prompt
    }

    @Test
    @DisplayName("Cycling puts the chosen basic land onto the battlefield untapped, then draws")
    void cyclingFetchesLandThenDraws() {
        setupAndCycle(List.of(new Forest(), new GrizzlyBears()));

        harness.handleMayAbilityChosen(player1, true); // accept -> library search
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest") && !p.isTapped());
        harness.assertInGraveyard(player1, "Shefet Monitor");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling search offers only basic land or Desert cards, put onto the battlefield untapped")
    void searchOffersOnlyBasicOrDesertCards() {
        setupAndCycle(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().cards()).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Declining the search skips the land but still draws a card")
    void decliningStillDraws() {
        setupAndCycle(List.of(new Forest(), new GrizzlyBears()));

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
        harness.assertInGraveyard(player1, "Shefet Monitor");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
