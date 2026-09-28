package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaspShrinkingSavior.class, GrizzlyBears.class})
class WaspShrinkingSaviorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking shrinks another creature and draws for creatures with negative power")
    void attackTriggerShrinksAndDrawsForNegativePower() {
        addReady(player1, new WaspShrinkingSavior());
        Permanent target = addReady(player2, new GrizzlyBears());
        Permanent zeroPowerCreature = addReady(player1, new GrizzlyBears());
        zeroPowerCreature.setPowerModifier(-2);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("The shrink lasts through cleanup and ends on the controller's next turn")
    void shrinkLastsUntilNextTurn() {
        addReady(player1, new WaspShrinkingSavior());
        Permanent target = addReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UNTAP);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack trigger may resolve without choosing a target")
    void mayChooseNoTarget() {
        addReady(player1, new WaspShrinkingSavior());
        Permanent target = addReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("The attack trigger cannot target Wasp itself")
    void cannotTargetSourceCreature() {
        Permanent wasp = addReady(player1, new WaspShrinkingSavior());
        addReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, wasp.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, Card card) {
        return addCreatureReady(player, card);
    }
}
