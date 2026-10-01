package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.d.DakmorSalvage;
import com.github.laxika.magicalvibes.cards.l.LlanowarEmpath;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PayManaOrLoseGameAtNextUpkeep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonersPact.class, DakmorSalvage.class, BlindPhantasm.class, LlanowarEmpath.class})
class SummonersPactTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a green creature and puts it into hand")
    void searchesForGreenCreature() {
        Card land = new DakmorSalvage();
        Card blueCreature = new BlindPhantasm();
        Card greenCreature = new LlanowarEmpath();
        harness.setLibrary(player1, List.of(land, blueCreature, greenCreature));
        castPact();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(greenCreature);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(greenCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, blueCreature);
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).singleElement()
                .satisfies(action -> {
                    assertThat(action.playerId()).isEqualTo(player1.getId());
                    assertThat(action.manaCost()).isEqualTo("{2}{G}{G}");
                });
    }

    @Test
    @DisplayName("A library without a green creature still schedules the upkeep payment")
    void canFailToFindCreature() {
        Card land = new DakmorSalvage();
        Card blueCreature = new BlindPhantasm();
        harness.setLibrary(player1, List.of(land, blueCreature));
        castPact();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, blueCreature);
    }

    @Test
    @DisplayName("Waits for the controller's next upkeep")
    void waitsForControllerNextUpkeep() {
        harness.setLibrary(player1, List.of());
        castPact();
        advanceToUpkeep(player2);

        assertThat(gd.currentStep).isEqualTo(TurnStep.UPKEEP);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).singleElement()
                .satisfies(action -> {
                    assertThat(action.playerId()).isEqualTo(player1.getId());
                    assertThat(action.manaCost()).isEqualTo("{2}{G}{G}");
                });
    }

    @Test
    @DisplayName("Paying {2}{G}{G} at the next upkeep avoids losing the game")
    void payingAtNextUpkeepAvoidsLoss() {
        harness.setLibrary(player1, List.of());
        castPact();
        reachNextUpkeepPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Being unable to pay at the next upkeep loses the game")
    void beingUnableToPayAtNextUpkeepCausesLoss() {
        harness.setLibrary(player1, List.of());
        castPact();
        reachNextUpkeepPrompt();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Declining the next-upkeep payment loses the game")
    void decliningAtNextUpkeepCausesLoss() {
        harness.setLibrary(player1, List.of());
        castPact();
        reachNextUpkeepPrompt();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private void castPact() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SummonersPact(), "{0}");
        harness.passBothPriorities();
    }

    private void reachNextUpkeepPrompt() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
