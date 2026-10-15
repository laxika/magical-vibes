package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OtterPenguin.class, GrizzlyBears.class})
class OtterPenguinTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+2 and can't be blocked when its controller draws their second card")
    void triggersOnSecondCardDraw() {
        Permanent otter = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        drawAndResolveTrigger(player1);
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(1);

        drawAndResolveTrigger(player1);
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(3);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        otter.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, otter)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Does not trigger on a first or third card draw")
    void triggersOnlyOnSecondCardDraw() {
        Permanent otter = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        drawAndResolveTrigger(player1);
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(2);

        drawAndResolveTrigger(player1);
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(3);

        drawAndResolveTrigger(player1);
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(3);
    }

    @Test
    @DisplayName("Controller's second draw triggers during an opponent's turn")
    void triggersDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        Permanent otter = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        harness.setLibrary(player1, List.of(new OtterPenguin(), new OtterPenguin()));

        drawAndResolveTrigger(player1);
        drawAndResolveTrigger(player1);

        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(3);
        assertThat(otter.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("An opponent's second draw does not trigger the ability")
    void doesNotTriggerForOpponentsDraws() {
        Permanent otter = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        harness.setLibrary(player2, List.of(new OtterPenguin(), new OtterPenguin()));

        drawAndResolveTrigger(player2);
        drawAndResolveTrigger(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(1);
        assertThat(otter.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Counts draws made before this creature entered the battlefield")
    void countsEarlierDrawsAndWaitsForResolution() {
        harness.setLibrary(player1, List.of(new OtterPenguin(), new OtterPenguin()));
        drawAndResolveTrigger(player1);
        Permanent otter = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(1);
        assertThat(otter.isCantBeBlocked()).isFalse();

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(3);
        assertThat(otter.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Both effects expire and the ability can trigger again on the next turn")
    void expiresAndTriggersAgainNextTurn() {
        Permanent otter = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        harness.setLibrary(player1, List.of(new OtterPenguin(), new OtterPenguin(),
                new OtterPenguin(), new OtterPenguin()));
        drawAndResolveTrigger(player1);
        drawAndResolveTrigger(player1);
        assertThat(otter.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(1);
        assertThat(otter.isCantBeBlocked()).isFalse();

        drawAndResolveTrigger(player1);
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(2);
        drawAndResolveTrigger(player1);
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(3);
        assertThat(otter.isCantBeBlocked()).isTrue();
    }

    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
