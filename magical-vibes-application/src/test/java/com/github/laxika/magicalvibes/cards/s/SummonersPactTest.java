package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PayManaOrLoseGameAtNextUpkeep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonersPact.class, Forest.class, GrizzlyBears.class, GoblinPiker.class})
class SummonersPactTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a green creature and puts it into hand")
    void searchesForGreenCreature() {
        Card forest = new Forest();
        Card goblin = new GoblinPiker();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, goblin, bears));
        castPact();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(bears);
        assertThat(search.params().reveals()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, goblin);
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).singleElement()
                .satisfies(action -> {
                    assertThat(action.playerId()).isEqualTo(player1.getId());
                    assertThat(action.manaCost()).isEqualTo("{2}{G}{G}");
                });
    }

    @Test
    @DisplayName("A library without a green creature still schedules the upkeep payment")
    void canFailToFindCreature() {
        Card forest = new Forest();
        Card goblin = new GoblinPiker();
        harness.setLibrary(player1, List.of(forest, goblin));
        castPact();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, goblin);
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
        harness.setHand(player1, List.of(new SummonersPact()));
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void reachNextUpkeepPrompt() {
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.turnNumber = 3;
        gd.activePlayerId = player1.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();
    }
}
