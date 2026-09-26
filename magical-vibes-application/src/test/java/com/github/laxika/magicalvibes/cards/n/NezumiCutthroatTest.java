package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.j.JadeIdol;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.cards.w.WickedAkuba;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NezumiCutthroat.class, HumbleBudoka.class, WickedAkuba.class, JadeIdol.class,
        ReachThroughMists.class})
class NezumiCutthroatTest extends BaseCardTest {

    @Test
    @DisplayName("Nezumi Cutthroat cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player1, new HumbleBudoka());
        addCreatureReady(player2, new NezumiCutthroat());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Fear stops a non-black, non-artifact creature from blocking Nezumi Cutthroat")
    void fearStopsGreenBlocker() {
        addCreatureReady(player1, new NezumiCutthroat());
        addCreatureReady(player2, new HumbleBudoka());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fear allows a black creature to block Nezumi Cutthroat")
    void fearAllowsBlackBlocker() {
        addCreatureReady(player1, new NezumiCutthroat());
        addCreatureReady(player2, new WickedAkuba());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Fear allows an artifact creature to block Nezumi Cutthroat")
    void fearAllowsArtifactBlocker() {
        addCreatureReady(player1, new NezumiCutthroat());
        addCreatureReady(player2, new JadeIdol());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new ReachThroughMists(), "{U}");
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
