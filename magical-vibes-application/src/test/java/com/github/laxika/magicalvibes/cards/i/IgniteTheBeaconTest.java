package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AjaniSteadfast;
import com.github.laxika.magicalvibes.cards.g.GideonJura;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IgniteTheBeacon.class, AjaniSteadfast.class, GideonJura.class, GrizzlyBears.class})
class IgniteTheBeaconTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for up to two planeswalker cards and puts the chosen cards into hand")
    void searchesForUpToTwoPlaneswalkers() {
        AjaniSteadfast ajani = new AjaniSteadfast();
        GideonJura gideon = new GideonJura();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ajani, bears, gideon));
        cast();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(ajani, gideon);
        assertThat(search.params().reveals()).isTrue();

        int ajaniIndex = search.params().cards().indexOf(ajani);
        assertThat(ajaniIndex).isGreaterThanOrEqualTo(0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(ajaniIndex));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(ajani, gideon);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        harness.assertInGraveyard(player1, "Ignite the Beacon");
    }

    @Test
    @DisplayName("Allows finding fewer than two planeswalker cards")
    void mayFindOnlyOnePlaneswalker() {
        AjaniSteadfast ajani = new AjaniSteadfast();
        GideonJura gideon = new GideonJura();
        harness.setLibrary(player1, List.of(ajani, gideon));
        cast();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        int ajaniIndex = search.params().cards().indexOf(ajani);
        assertThat(ajaniIndex).isGreaterThanOrEqualTo(0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(ajaniIndex));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).contains(ajani).doesNotContain(gideon);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(gideon);
    }

    private void cast() {
        harness.setHand(player1, List.of(new IgniteTheBeacon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
