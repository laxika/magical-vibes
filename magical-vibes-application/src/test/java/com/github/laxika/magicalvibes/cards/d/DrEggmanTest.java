package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MetalworkColossus;
import com.github.laxika.magicalvibes.cards.s.SmugglersCopter;
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

@CardUsed({DrEggman.class, UltronDrone.class, MetalworkColossus.class, SmugglersCopter.class})
class DrEggmanTest extends BaseCardTest {

    @Test
    @DisplayName("draws a card and lets an opponent choose to discard")
    void drawsAndOpponentDiscards() {
        DrEggman eggman = new DrEggman();
        var discarded = new DrEggman();
        var drawn = new DrEggman();
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
        harness.setLibrary(player1, List.of(new DrEggman()));
        harness.setHand(player2, List.of(new DrEggman()));

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

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        var drawn = new DrEggman();
        harness.addToBattlefield(player1, new DrEggman());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of(new DrEggman()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(drawn);
    }

    @Test
    void emptyHandOpponentCanChooseDiscardWithoutPuttingACardOntoBattlefield() {
        var construct = new MetalworkColossus();
        harness.addToBattlefield(player1, new DrEggman());
        harness.setHand(player1, List.of(construct));
        harness.setLibrary(player1, List.of(new DrEggman()));
        harness.setHand(player2, List.of());

        resolveEndStep();

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains(ChoiceContext.VillainousChoice.DISCARD);
        harness.handleListChoice(player2, ChoiceContext.VillainousChoice.DISCARD);

        assertThat(gd.playerHands.get(player1.getId())).contains(construct);
        harness.assertNotOnBattlefield(player1, "Metalwork Colossus");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentCanChoosePutWhenControllerHasNoEligibleCard() {
        var opponentCard = new DrEggman();
        harness.addToBattlefield(player1, new DrEggman());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new DrEggman()));
        harness.setHand(player2, List.of(opponentCard));

        resolveEndStep();

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        harness.handleListChoice(player2, putOption(choice));

        assertThat(gd.playerHands.get(player2.getId())).contains(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerCanDeclinePuttingAnEligibleCardOntoBattlefield() {
        var construct = new MetalworkColossus();
        var opponentCard = new DrEggman();
        harness.addToBattlefield(player1, new DrEggman());
        harness.setHand(player1, List.of(construct));
        harness.setLibrary(player1, List.of(new DrEggman()));
        harness.setHand(player2, List.of(opponentCard));

        resolveEndStep();

        harness.handleListChoice(player2, putOption(
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(construct);
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentCard);
        harness.assertNotOnBattlefield(player1, "Metalwork Colossus");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void newlyDrawnConstructCanBePutOntoBattlefield() {
        var construct = new MetalworkColossus();
        harness.addToBattlefield(player1, new DrEggman());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(construct));
        harness.setHand(player2, List.of(new DrEggman()));

        resolveEndStep();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(construct);
        harness.handleListChoice(player2, putOption(
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)));
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Metalwork Colossus");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void vehicleIsEligibleButUnrelatedCreatureIsNot() {
        var vehicle = new SmugglersCopter();
        var unrelated = new DrEggman();
        harness.addToBattlefield(player1, new DrEggman());
        harness.setHand(player1, List.of(unrelated, vehicle));
        harness.setLibrary(player1, List.of(new DrEggman()));
        harness.setHand(player2, List.of(new DrEggman()));

        resolveEndStep();

        harness.handleListChoice(player2, putOption(
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)));
        harness.handleMayAbilityChosen(player1, true);
        var choice = gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Smuggler's Copter");
        assertThat(gd.playerHands.get(player1.getId())).contains(unrelated).doesNotContain(vehicle);
        assertThat(gd.stack).isEmpty();
    }

    private String putOption(PendingInteraction.ColorChoice choice) {
        return choice.options().stream()
                .filter(option -> !option.equals(ChoiceContext.VillainousChoice.DISCARD))
                .findFirst().orElseThrow();
    }

    private void resolveEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
