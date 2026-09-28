package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UltronDrone;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrEggman.class, GrizzlyBears.class, UltronDrone.class})
class DrEggmanTest extends BaseCardTest {

    @Test
    @DisplayName("draws a card and lets an opponent choose to discard")
    void drawsAndOpponentDiscards() {
        DrEggman eggman = new DrEggman();
        var discarded = new GrizzlyBears();
        var drawn = new GrizzlyBears();
        harness.addToBattlefield(player1, eggman);
        harness.setHand(player1, List.of(new UltronDrone()));
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of(discarded));

        resolveEndStep();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).contains(ChoiceContext.VillainousChoice.DISCARD);
        harness.handleListChoice(player2, ChoiceContext.VillainousChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    @Test
    @DisplayName("lets the controller put a matching card onto the battlefield")
    void controllerPutsMatchingCardOntoBattlefield() {
        DrEggman eggman = new DrEggman();
        var robot = new UltronDrone();
        harness.addToBattlefield(player1, eggman);
        harness.setHand(player1, List.of(robot));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        resolveEndStep();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        String putOption = choice.options().stream()
                .filter(option -> !option.equals(ChoiceContext.VillainousChoice.DISCARD))
                .findFirst().orElseThrow();
        harness.handleListChoice(player2, putOption);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.HandCardChoice cardChoice =
                gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(cardChoice.playerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, cardChoice.validIndices().getFirst());

        harness.assertOnBattlefield(player1, "Ultron Drone");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(robot);
    }

    private void resolveEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
