package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShannaPurifyingBlade.class, GrizzlyBears.class})
class ShannaPurifyingBladeTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, X is capped at life gained this turn and draws X cards")
    void paysUpToLifeGainedAndDrawsCards() {
        harness.addToBattlefield(player1, new ShannaPurifyingBlade());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        advanceToEndStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.passBothPriorities();

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isEqualTo(2);

        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Does not draw when no mana is available")
    void noManaDoesNotPrompt() {
        harness.addToBattlefield(player1, new ShannaPurifyingBlade());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Triggers only during Shanna's controller's end step")
    void onlyTriggersOnControllerEndStep() {
        harness.addToBattlefield(player1, new ShannaPurifyingBlade());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        advanceToEndStep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Lifelink combat damage supplies the end-step draw limit")
    void lifelinkSuppliesDrawLimit() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ShannaPurifyingBlade());
        harness.setLibrary(player1, List.of(new ShannaPurifyingBlade(),
                new ShannaPurifyingBlade(), new ShannaPurifyingBlade()));

        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            declareAttackers(List.of(0));
            resolveCombat();
            harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        });

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        advanceToEndStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).maxValue())
                .isEqualTo(3);
        harness.handleXValueChosen(player1, 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    @DisplayName("The controller may decline payment despite having life gain and mana")
    void mayDeclinePayment() {
        harness.addToBattlefield(player1, new ShannaPurifyingBlade());
        harness.setLibrary(player1, List.of(new ShannaPurifyingBlade()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's life gain does not permit a positive payment")
    void opponentsLifeGainDoesNotCount() {
        harness.addToBattlefield(player1, new ShannaPurifyingBlade());
        harness.setLibrary(player1, List.of(new ShannaPurifyingBlade()));
        gd.lifeGainedThisTurn.put(player2.getId(), 5);

        advanceToEndStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Life gained after the ability triggers counts when it resolves")
    void lifeGainLimitIsEvaluatedAtResolution() {
        harness.addToBattlefield(player1, new ShannaPurifyingBlade());
        harness.setLibrary(player1, List.of(new ShannaPurifyingBlade(), new ShannaPurifyingBlade()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).maxValue())
                .isEqualTo(2);
        harness.handleXValueChosen(player1, 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("The triggered ability still draws after Shanna leaves the battlefield")
    void triggerSurvivesSourceLeaving() {
        harness.addToBattlefield(player1, new ShannaPurifyingBlade());
        harness.setLibrary(player1, List.of(new ShannaPurifyingBlade()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
