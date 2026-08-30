package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GibberingDescent.class, GrizzlyBears.class, AngelOfMercy.class, RavensCrime.class})
class GibberingDescentTest extends BaseCardTest {

    @Test
    @DisplayName("Each player's upkeep makes that player lose 1 life and discard a card")
    void eachPlayersUpkeepCausesLifeLossAndDiscard() {
        harness.addToBattlefield(player1, new GibberingDescent());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new AngelOfMercy()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Angel of Mercy");
    }

    @Test
    @DisplayName("Hellbent skips only the controller's upkeep when their hand is empty")
    void hellbentSkipsOnlyControllerUpkeep() {
        harness.addToBattlefield(player1, new GibberingDescent());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Discarding Gibbering Descent offers its madness cost")
    void discardOffersMadnessCast() {
        GibberingDescent descent = new GibberingDescent();
        harness.setHand(player1, List.of(descent));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(descent.getId()));
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
