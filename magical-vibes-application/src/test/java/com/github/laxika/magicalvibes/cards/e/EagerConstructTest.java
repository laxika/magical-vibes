package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EagerConstructTest extends BaseCardTest {

    @Test
    @DisplayName("Each player may decline the ETB scry in active-player-first order")
    void eachPlayerMayDeclineInApnapOrder() {
        Card player1Top = new Spellbook();
        Card player1Bottom = new GrizzlyBears();
        Card player2Top = new Spellbook();
        Card player2Bottom = new GrizzlyBears();
        harness.setHand(player1, List.of(new EagerConstruct()));
        harness.setLibrary(player1, List.of(player1Top, player1Bottom));
        harness.setLibrary(player2, List.of(player2Top, player2Bottom));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactly(player1Top, player1Bottom);
        assertThat(harness.getGameData().playerDecks.get(player2.getId())).containsExactly(player2Top, player2Bottom);
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Each player's accepted ETB choice scries that player's library")
    void eachPlayerMayScryTheirOwnLibrary() {
        Card player1Top = new Spellbook();
        Card player1Bottom = new GrizzlyBears();
        Card player2Top = new Spellbook();
        Card player2Bottom = new GrizzlyBears();
        harness.setHand(player1, List.of(new EagerConstruct()));
        harness.setLibrary(player1, List.of(player1Top, player1Bottom));
        harness.setLibrary(player2, List.of(player2Top, player2Bottom));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(harness.getGameData().interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(harness.getGameData().interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactly(player1Bottom, player1Top);
        assertThat(harness.getGameData().playerDecks.get(player2.getId())).containsExactly(player2Bottom, player2Top);
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
    }
}
