package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
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
        addCreatureReady(player1, new WaspShrinkingSavior());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent zeroPowerCreature = addCreatureReady(player1, new GrizzlyBears());
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
        addCreatureReady(player1, new WaspShrinkingSavior());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
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
        addCreatureReady(player1, new WaspShrinkingSavior());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
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
        Permanent wasp = addCreatureReady(player1, new WaspShrinkingSavior());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, wasp.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Choosing no target still draws for negative-power creatures on both battlefields, including Wasp")
    void noTargetDrawsForAllNegativePowerCreatures() {
        Permanent wasp = addCreatureReady(player1, new WaspShrinkingSavior());
        wasp.setPowerModifier(-2);
        Permanent opposingWasp = addCreatureReady(player2, new WaspShrinkingSavior());
        opposingWasp.setPowerModifier(-2);
        harness.setLibrary(player1, List.of(new WaspShrinkingSavior(), new WaspShrinkingSavior()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gqs.getEffectivePower(gd, opposingWasp)).isEqualTo(-1);
    }

    @Test
    @DisplayName("A creature reduced to exactly zero power does not count for drawing")
    void zeroPowerAfterShrinkDoesNotDraw() {
        addCreatureReady(player1, new WaspShrinkingSavior());
        Permanent target = addCreatureReady(player2, new WaspShrinkingSavior());
        target.setPowerModifier(2);
        harness.setLibrary(player1, List.of(new WaspShrinkingSavior()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Losing the only chosen target prevents drawing even with a negative-power creature remaining")
    void removedTargetPreventsDraw() {
        Permanent wasp = addCreatureReady(player1, new WaspShrinkingSavior());
        wasp.setPowerModifier(-2);
        Permanent target = addCreatureReady(player2, new WaspShrinkingSavior());
        harness.setLibrary(player1, List.of(new WaspShrinkingSavior()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }
}
