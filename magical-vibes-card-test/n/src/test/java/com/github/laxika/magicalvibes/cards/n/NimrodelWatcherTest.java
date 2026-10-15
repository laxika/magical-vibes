package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NimrodelWatcher.class, Opt.class, GrizzlyBears.class})
class NimrodelWatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Scrying gives Nimrodel Watcher +1/+0 and makes it unblockable")
    void scryBoostsAndMakesUnblockable() {
        Permanent watcher = addWatcher();

        scryWithOpt();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        finishScry();

        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(1);
        assertThat(watcher.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Nimrodel Watcher's scry ability triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        Permanent watcher = addWatcher();
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Opt(), new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        finishScry();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        finishScry();

        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nimrodel Watcher's scry bonus wears off at end of turn")
    void bonusWearsOffAtEndOfTurn() {
        Permanent watcher = addWatcher();

        scryWithOpt();
        finishScry();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(2);
        assertThat(watcher.isCantBeBlocked()).isFalse();
    }

    private Permanent addWatcher() {
        Permanent watcher = harness.addToBattlefieldAndReturn(player1, new NimrodelWatcher());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return watcher;
    }

    private void scryWithOpt() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void finishScry() {
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        resolveAllTriggers();
    }
}
