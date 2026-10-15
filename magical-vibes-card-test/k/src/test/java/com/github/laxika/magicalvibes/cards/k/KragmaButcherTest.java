package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KragmaButcher.class})
class KragmaButcherTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming untapped gives Kragma Butcher +2/+0 until end of turn")
    void becomingUntappedBoostsItUntilEndOfTurn() {
        Permanent butcher = addTappedButcher(player1);
        int basePower = gqs.getEffectivePower(gd, butcher);
        int baseToughness = gqs.getEffectiveToughness(gd, butcher);

        runUntapStep(player1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, butcher)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, butcher)).isEqualTo(baseToughness);

        harness.setHand(player1, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, butcher)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("An already untapped Butcher does not trigger during untap")
    void alreadyUntappedDoesNotGetBoosted() {
        Permanent butcher = harness.addToBattlefieldAndReturn(player1, new KragmaButcher());
        int basePower = gqs.getEffectivePower(gd, butcher);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, butcher)).isEqualTo(basePower);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Butcher boosts only itself when it becomes untapped")
    void untappingOneButcherDoesNotBoostOtherCopies() {
        Permanent tapped = addTappedButcher(player1);
        Permanent untapped = harness.addToBattlefieldAndReturn(player1, new KragmaButcher());
        Permanent opposing = addTappedButcher(player2);
        int tappedPower = gqs.getEffectivePower(gd, tapped);
        int untappedPower = gqs.getEffectivePower(gd, untapped);
        int opposingPower = gqs.getEffectivePower(gd, opposing);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, tapped)).isEqualTo(tappedPower + 2);
        assertThat(gqs.getEffectivePower(gd, untapped)).isEqualTo(untappedPower);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(opposingPower);
        assertThat(opposing.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Multiple untap events accumulate boosts in the same turn")
    void repeatedUntapEventsAccumulate() {
        Permanent butcher = addTappedButcher(player1);
        int basePower = gqs.getEffectivePower(gd, butcher);
        int baseToughness = gqs.getEffectiveToughness(gd, butcher);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        butcher.tap();
        harness.performUntapStep(player1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, butcher)).isEqualTo(basePower + 4);
        assertThat(gqs.getEffectiveToughness(gd, butcher)).isEqualTo(baseToughness);
    }

    private Permanent addTappedButcher(Player player) {
        Permanent butcher = harness.addToBattlefieldAndReturn(player, new KragmaButcher());
        butcher.setSummoningSick(false);
        butcher.tap();
        return butcher;
    }

    private void runUntapStep(Player untappingPlayer) {
        Player opponent = untappingPlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(opponent);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
