package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyrCustodian.class, Spellbook.class, GrizzlyBears.class})
class MyrCustodianTest extends BaseCardTest {

    @Test
    @DisplayName("ETB scries 2, then the opponent may scry 1")
    void scriesThenOffersOpponentScry() {
        Card player1Top = new Spellbook();
        Card player1Second = new GrizzlyBears();
        Card player1Third = new Spellbook();
        Card player2Top = new Spellbook();
        Card player2Bottom = new GrizzlyBears();
        harness.setLibrary(player1, List.of(player1Top, player1Second, player1Third));
        harness.setLibrary(player2, List.of(player2Top, player2Bottom));
        harness.castFromHand(player1, new MyrCustodian(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .containsExactly(player1Second, player1Third, player1Top);
        assertThat(harness.getGameData().playerDecks.get(player2.getId()))
                .containsExactly(player2Bottom, player2Top);
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The opponent may decline the ETB scry")
    void opponentMayDeclineScry() {
        Card player1Top = new Spellbook();
        Card player1Bottom = new GrizzlyBears();
        Card player2Top = new Spellbook();
        Card player2Bottom = new GrizzlyBears();
        harness.setLibrary(player1, List.of(player1Top, player1Bottom));
        harness.setLibrary(player2, List.of(player2Top, player2Bottom));
        harness.castFromHand(player1, new MyrCustodian(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .containsExactly(player1Bottom, player1Top);
        assertThat(harness.getGameData().playerDecks.get(player2.getId()))
                .containsExactly(player2Top, player2Bottom);
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty controller library still allows the opponent to scry")
    void emptyControllerLibraryStillOffersOpponentScry() {
        Card opponentTop = new MyrCustodian();
        Card opponentSecond = new MyrCustodian();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(opponentTop, opponentSecond));
        harness.castFromHand(player1, new MyrCustodian(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentSecond, opponentTop);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Scry 2 handles a one-card library and an opponent with an empty library")
    void shortControllerLibraryAndEmptyOpponentLibrary() {
        Card controllerTop = new MyrCustodian();
        harness.setLibrary(player1, List.of(controllerTop));
        harness.setLibrary(player2, List.of());
        harness.castFromHand(player1, new MyrCustodian(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerTop);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
