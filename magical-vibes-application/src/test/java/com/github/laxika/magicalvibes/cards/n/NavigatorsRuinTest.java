package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NavigatorsRuin.class})
class NavigatorsRuinTest extends BaseCardTest {

    @Test
    @DisplayName("When raid met, target opponent mills 4 cards")
    void raidMetMillsOpponent() {
        harness.addToBattlefield(player1, new NavigatorsRuin());

        int graveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        beginRaidEndStep();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Select opponent as target
        harness.handlePermanentChosen(player1, player2.getId());

        // Triggered ability is now on the stack — resolve it
        harness.passBothPriorities();

        // Opponent should have milled 4 cards into graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()).size())
                .isGreaterThanOrEqualTo(graveyardBefore + 4);
    }

    @Test
    @DisplayName("When raid not met (did not attack), end step trigger does not fire")
    void raidNotMetNoTrigger() {
        harness.addToBattlefield(player1, new NavigatorsRuin());

        int graveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        // Do NOT mark attacked this turn
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // No mill — raid condition not met; graveyard should be unchanged
        assertThat(gd.playerGraveyards.get(player2.getId()).size()).isEqualTo(graveyardBefore);
    }

    @Test
    @DisplayName("Does not trigger on opponent's end step even if controller attacked")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new NavigatorsRuin());

        // Mark player1 attacked, but it's player2's turn
        markAttackedThisTurn();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        // No trigger for player1's Navigator's Ruin on player2's end step
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    @Test
    @DisplayName("The enchantment can be cast without targeting an opponent")
    void castsWithoutTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new NavigatorsRuin(), "{2}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Navigator's Ruin");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Raid mills exactly the top four cards and leaves the controller's library alone")
    void millsExactlyTopFour() {
        var cards = List.of(new NavigatorsRuin(), new NavigatorsRuin(), new NavigatorsRuin(),
                new NavigatorsRuin(), new NavigatorsRuin());
        harness.setLibrary(player2, cards);
        var controllerLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));
        harness.addToBattlefield(player1, new NavigatorsRuin());
        beginRaidEndStep();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(cards.subList(0, 4));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(cards.get(4));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(controllerLibrary);
    }

    @Test
    @DisplayName("Raid mills all remaining cards when the opponent has fewer than four")
    void millsShortLibrary() {
        var cards = List.of(new NavigatorsRuin(), new NavigatorsRuin());
        harness.setLibrary(player2, cards);
        harness.addToBattlefield(player1, new NavigatorsRuin());
        beginRaidEndStep();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(cards);
    }

    @Test
    @DisplayName("An opponent with an empty library is still a legal target")
    void emptyLibraryIsLegalTarget() {
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new NavigatorsRuin());
        beginRaidEndStep();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The raid trigger cannot target its controller")
    void cannotTargetController() {
        harness.addToBattlefield(player1, new NavigatorsRuin());
        beginRaidEndStep();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The raid trigger resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        harness.addToBattlefield(player1, new NavigatorsRuin());
        beginRaidEndStep();
        harness.handlePermanentChosen(player1, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        int before = gd.playerGraveyards.get(player2.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(before + 4);
    }

    private void beginRaidEndStep() {
        markAttackedThisTurn();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
