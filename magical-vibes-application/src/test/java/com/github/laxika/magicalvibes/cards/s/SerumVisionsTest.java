package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RazorgrassScreen;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerumVisions.class, RazorgrassScreen.class})
class SerumVisionsTest extends BaseCardTest {

    @Test
    void drawsBeforeScriesTwo() {
        Card drawn = new RazorgrassScreen();
        Card scryTop = new RazorgrassScreen();
        Card scryBottom = new RazorgrassScreen();
        SerumVisions spell = new SerumVisions();
        harness.setLibrary(player1, List.of(drawn, scryTop, scryBottom));

        harness.castFromHand(player1, spell, "{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(scryTop, scryBottom);
    }

    @Test
    void scriesTwoAndFinishesResolving() {
        Card drawn = new RazorgrassScreen();
        Card scryTop = new RazorgrassScreen();
        Card scryBottom = new RazorgrassScreen();
        SerumVisions spell = new SerumVisions();
        harness.setLibrary(player1, List.of(drawn, scryTop, scryBottom));

        harness.castFromHand(player1, spell, "{U}");
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(scryBottom, scryTop);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    void scriesTwoCanPutOneCardOnBottom() {
        Card drawn = new RazorgrassScreen();
        Card scryTop = new RazorgrassScreen();
        Card scryBottom = new RazorgrassScreen();
        SerumVisions spell = new SerumVisions();
        harness.setLibrary(player1, List.of(drawn, scryTop, scryBottom));

        harness.castFromHand(player1, spell, "{U}");
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(scryBottom, scryTop);
        assertThat(gd.stack).isEmpty();
    }
}
