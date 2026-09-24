package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnbreakReclaimer.class, GrizzlyBears.class})
class DawnbreakReclaimerTest extends BaseCardTest {

    @Test
    void choosesAcrossGraveyardsThenReturnsUnderOwnersControl() {
        Card opponentChoice = new GrizzlyBears();
        Card opponentOther = new GrizzlyBears();
        Card controllerChoice = new GrizzlyBears();
        Card controllerOther = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentChoice, opponentOther));
        harness.setGraveyard(player1, List.of(controllerChoice, controllerOther));
        harness.addToBattlefield(player1, new DawnbreakReclaimer());

        advanceToOwnEndStep();

        PendingInteraction.GraveyardChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        harness.handleGraveyardCardChosen(player1, cardPoolIndex(firstChoice, opponentChoice));

        PendingInteraction.GraveyardChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        harness.handleGraveyardCardChosen(player2, cardPoolIndex(secondChoice, controllerChoice));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(controllerChoice.getId())))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(opponentChoice.getId())))
                .hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void decliningMayLeavesBothCardsInTheirGraveyards() {
        Card opponentChoice = new GrizzlyBears();
        Card controllerChoice = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentChoice));
        harness.setGraveyard(player1, List.of(controllerChoice));
        harness.addToBattlefield(player1, new DawnbreakReclaimer());

        advanceToOwnEndStep();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(controllerChoice.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .contains(opponentChoice.getId());
    }

    private int cardPoolIndex(PendingInteraction.GraveyardChoice choice, Card card) {
        return choice.cardPool().stream()
                .map(Card::getId)
                .toList()
                .indexOf(card.getId());
    }

    private void advanceToOwnEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
