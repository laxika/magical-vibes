package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FecundityTest extends BaseCardTest {

    // ===== Dying creature's controller (an opponent) may draw =====

    @Test
    @DisplayName("When an opponent's creature dies, that opponent may draw (accept)")
    void opponentsCreatureDiesOpponentDraws() {
        harness.addToBattlefield(player1, new Fecundity());
        harness.addToBattlefield(player2, new GrizzlyBears());

        setDeck(player2, List.of(new com.github.laxika.magicalvibes.cards.f.Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve Cruel Edict → player2 sacrifices Grizzly Bears
        harness.passBothPriorities(); // resolve Fecundity trigger → may prompt

        // The DYING creature's controller (player2), not Fecundity's controller, is offered the draw.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Dying creature's controller may decline the draw")
    void controllerMayDecline() {
        harness.addToBattlefield(player1, new Fecundity());
        harness.addToBattlefield(player2, new GrizzlyBears());

        setDeck(player2, List.of(new com.github.laxika.magicalvibes.cards.f.Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore);
    }

    // ===== Also fires for the controller's own creatures =====

    @Test
    @DisplayName("When Fecundity's controller's creature dies, that controller may draw")
    void ownCreatureDiesControllerDraws() {
        harness.addToBattlefield(player1, new Fecundity());
        harness.addToBattlefield(player1, new GrizzlyBears());

        setDeck(player1, List.of(new com.github.laxika.magicalvibes.cards.f.Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities(); // resolve Cruel Edict → player1 sacrifices Grizzly Bears
        harness.passBothPriorities(); // resolve Fecundity trigger → may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    // ===== Helpers =====

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
