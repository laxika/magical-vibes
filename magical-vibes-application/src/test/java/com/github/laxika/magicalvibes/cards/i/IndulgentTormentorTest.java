package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndulgentTormentorTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent can let the controller draw a card")
    void opponentLetsControllerDraw() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest));
        addCreatureReady(player1, new IndulgentTormentor());

        resolveTormentorTrigger();
        harness.handleListChoice(player2, ChoiceContext.IndulgentTormentorChoice.DRAW);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The opponent can pay three life")
    void opponentPaysLife() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new IndulgentTormentor());

        resolveTormentorTrigger();
        harness.handleListChoice(player2, ChoiceContext.IndulgentTormentorChoice.payLife(3));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The opponent chooses which creature to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new IndulgentTormentor());

        resolveTormentorTrigger();
        harness.handleListChoice(player2, ChoiceContext.IndulgentTormentorChoice.SACRIFICE);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNotNull();
        harness.handlePermanentChosen(player2, first.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("If sacrifice and payment are impossible, the effect draws automatically")
    void impossibleOptionsArePruned() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest));
        harness.setLife(player2, 2);
        addCreatureReady(player1, new IndulgentTormentor());

        resolveTormentorTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(2);
    }

    private void resolveTormentorTrigger() {
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }
}
