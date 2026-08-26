package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed({FolioOfFancies.class, GrizzlyBears.class})
class FolioOfFanciesTest extends BaseCardTest {

    @Test
    @DisplayName("Controller has no maximum hand size")
    void controllerHasNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new FolioOfFancies());
        harness.setHand(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()
        ));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    @DisplayName("Opponent has no maximum hand size")
    void opponentHasNoMaximumHandSize() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new FolioOfFancies());
        harness.setHand(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()
        ));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(9);
    }

    @Test
    @DisplayName("X X and tap makes each player draw X cards")
    void eachPlayerDrawsXCards() {
        harness.addToBattlefield(player1, new FolioOfFancies());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        int player1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int player2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckBefore - 2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckBefore - 2);
    }

    @Test
    @DisplayName("Each opponent mills their hand size")
    void eachOpponentMillsTheirHandSize() {
        harness.addToBattlefield(player1, new FolioOfFancies());
        harness.setHand(player2, List.of());
        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.setHand(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()
        ));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckBefore - 4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }
}
