package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BorderPatrol;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BorderPatrol.class, HaplessResearcher.class, SuntailHawk.class})
class HaplessResearcherTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing it draws a card, then discards a card")
    void sacrificesDrawsThenDiscards() {
        harness.addToBattlefield(player1, new HaplessResearcher());
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.setLibrary(player1, List.of(new BorderPatrol()));

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Hapless Researcher");
        harness.assertInGraveyard(player1, "Hapless Researcher");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.assertInHand(player1, "Suntail Hawk");
        harness.assertInHand(player1, "Border Patrol");
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Border Patrol");
        harness.assertInGraveyard(player1, "Suntail Hawk");
    }

    @Test
    @DisplayName("Draws the only available card before discarding it")
    void drawsThenDiscardsWhenHandStartsEmpty() {
        harness.addToBattlefield(player1, new HaplessResearcher());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BorderPatrol()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertNotInHand(player1, "Border Patrol");
        harness.assertInGraveyard(player1, "Border Patrol");
    }

    @Test
    @DisplayName("May discard the drawn card instead of a card already in hand")
    void mayDiscardTheDrawnCard() {
        harness.addToBattlefield(player1, new HaplessResearcher());
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.setLibrary(player1, List.of(new BorderPatrol()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Suntail Hawk");
        harness.assertNotInHand(player1, "Border Patrol");
        harness.assertInGraveyard(player1, "Border Patrol");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick on the opponent's turn")
    void activatesWhileTappedAndSummoningSickOnOpponentsTurn() {
        var researcher = harness.addToBattlefieldAndReturn(player1, new HaplessResearcher());
        researcher.tap();
        researcher.setSummoningSick(true);
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.setLibrary(player1, List.of(new BorderPatrol()));
        harness.setHand(player2, List.of(new SuntailHawk()));
        harness.setLibrary(player2, List.of(new BorderPatrol()));

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Hapless Researcher");
        harness.assertNotInHand(player1, "Border Patrol");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Border Patrol");
        harness.assertInGraveyard(player1, "Suntail Hawk");
        harness.assertInHand(player2, "Suntail Hawk");
        harness.assertNotInHand(player2, "Border Patrol");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}
