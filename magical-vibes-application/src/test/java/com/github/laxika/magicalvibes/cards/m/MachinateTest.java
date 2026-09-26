package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Machinate.class, DarksteelCitadel.class, CrazedGoblin.class})
class MachinateTest extends BaseCardTest {

    @Test
    void looksAtAsManyCardsAsArtifactsYouControl() {
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.addToBattlefield(player1, new DarksteelCitadel());

        Card chosen = new DarksteelCitadel();
        Card bottomed = new DarksteelCitadel();
        Card otherBottomed = new DarksteelCitadel();
        Card untouched = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(chosen, bottomed, otherBottomed, untouched));

        harness.castFromHand(player1, new Machinate(), "{1}{U}{U}");
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, otherBottomed, bottomed);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void controlsNoArtifactsLeavesLibraryUnchanged() {
        Card topCard = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(topCard));

        harness.castFromHand(player1, new Machinate(), "{1}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void countsOnlyArtifactsControlledByTheCaster() {
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.addToBattlefield(player1, new CrazedGoblin());
        harness.addToBattlefield(player2, new DarksteelCitadel());

        Card topCard = new DarksteelCitadel();
        Card untouched = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(topCard, untouched));

        harness.castFromHand(player1, new Machinate(), "{1}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void shortLibraryPutsAvailableCardIntoHand() {
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.addToBattlefield(player1, new DarksteelCitadel());

        Card onlyCard = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(onlyCard));

        harness.castFromHand(player1, new Machinate(), "{1}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDoesNothingWhenArtifactsAreControlled() {
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new Machinate(), "{1}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
