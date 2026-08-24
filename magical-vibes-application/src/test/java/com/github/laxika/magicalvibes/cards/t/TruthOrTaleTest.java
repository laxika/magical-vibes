package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TruthOrTale.class, Shock.class, GiantGrowth.class, GrizzlyBears.class})
class TruthOrTaleTest extends BaseCardTest {

    @Test
    @DisplayName("controller separates, opponent chooses a pile, and controller chooses one card")
    void opponentChoosesPileThenControllerChoosesOneCard() {
        Card shock = new Shock();
        Card giantGrowth = new GiantGrowth();
        Card bears = new GrizzlyBears();
        Card secondShock = new Shock();
        Card secondGrowth = new GiantGrowth();
        harness.setLibrary(player1, List.of(shock, giantGrowth, bears, secondShock, secondGrowth));

        cast();

        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId(), giantGrowth.getId()));
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.MultiGraveyardChoice cardChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(cardChoice.playerId()).isEqualTo(player1.getId());
        assertThat(cardChoice.validCardIds()).containsExactly(shock.getId(), giantGrowth.getId());

        harness.handleMultipleCardsChosen(player1, List.of(giantGrowth.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3)));

        assertThat(gd.playerHands.get(player1.getId())).contains(giantGrowth);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(shock, bears, secondShock, secondGrowth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears, secondShock, secondGrowth, shock);
    }

    @Test
    @DisplayName("the opponent may choose the other pile")
    void opponentChoosesOtherPile() {
        Card shock = new Shock();
        Card giantGrowth = new GiantGrowth();
        Card bears = new GrizzlyBears();
        Card secondShock = new Shock();
        Card secondGrowth = new GiantGrowth();
        harness.setLibrary(player1, List.of(shock, giantGrowth, bears, secondShock, secondGrowth));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId(), giantGrowth.getId()));
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 3, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondShock, secondGrowth, giantGrowth, shock);
    }

    private void cast() {
        harness.setHand(player1, List.of(new TruthOrTale()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
