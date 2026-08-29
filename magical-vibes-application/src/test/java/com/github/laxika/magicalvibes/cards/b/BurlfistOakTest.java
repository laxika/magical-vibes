package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BurlfistOakTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a card gives Burlfist Oak +2/+2 until end of turn")
    void drawingCardBoostsSelf() {
        Permanent oak = addOak(player1);
        addCardToDeck(player1);

        drawAndResolveTrigger(player1);

        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(5);
    }

    @Test
    @DisplayName("Each card drawn gives Burlfist Oak another +2/+2")
    void triggersOncePerCardDrawn() {
        Permanent oak = addOak(player1);
        addCardToDeck(player1);
        addCardToDeck(player1);

        drawAndResolveTrigger(player1);
        drawAndResolveTrigger(player1);

        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(7);
    }

    @Test
    @DisplayName("An opponent drawing a card does not trigger Burlfist Oak")
    void opponentDrawDoesNotTrigger() {
        Permanent oak = addOak(player1);
        addCardToDeck(player2);

        drawAndResolveTrigger(player2);

        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent oak = addOak(player1);
        harness.setHand(player1, List.of());
        addCardToDeck(player1);

        drawAndResolveTrigger(player1);
        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, oak)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oak)).isEqualTo(3);
    }

    private Permanent addOak(Player player) {
        return harness.addToBattlefieldAndReturn(player, new BurlfistOak());
    }

    private void addCardToDeck(Player player) {
        gd.playerDecks.get(player.getId()).add(new GrizzlyBears());
    }

    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
