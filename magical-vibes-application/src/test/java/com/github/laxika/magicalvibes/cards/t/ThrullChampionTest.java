package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BasalThrull;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThrullChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Thrull creatures on all battlefields get +1/+1")
    void boostsThrullsOnAllBattlefields() {
        Permanent ownThrull = addCreatureReady(player1, new BasalThrull());
        Permanent opponentThrull = addCreatureReady(player2, new BasalThrull());
        Permanent nonThrull = addCreatureReady(player2, new GrizzlyBears());

        int ownPowerBefore = gqs.getEffectivePower(gd, ownThrull);
        int ownToughnessBefore = gqs.getEffectiveToughness(gd, ownThrull);
        int opponentPowerBefore = gqs.getEffectivePower(gd, opponentThrull);
        int opponentToughnessBefore = gqs.getEffectiveToughness(gd, opponentThrull);
        int nonThrullPowerBefore = gqs.getEffectivePower(gd, nonThrull);
        int nonThrullToughnessBefore = gqs.getEffectiveToughness(gd, nonThrull);

        addCreatureReady(player1, new ThrullChampion());

        assertThat(gqs.getEffectivePower(gd, ownThrull)).isEqualTo(ownPowerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownThrull)).isEqualTo(ownToughnessBefore + 1);
        assertThat(gqs.getEffectivePower(gd, opponentThrull)).isEqualTo(opponentPowerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, opponentThrull))
                .isEqualTo(opponentToughnessBefore + 1);
        assertThat(gqs.getEffectivePower(gd, nonThrull)).isEqualTo(nonThrullPowerBefore);
        assertThat(gqs.getEffectiveToughness(gd, nonThrull)).isEqualTo(nonThrullToughnessBefore);
    }

    @Test
    @DisplayName("Tapping Thrull Champion gains control of a target Thrull")
    void gainsControlOfTargetThrull() {
        Permanent champion = addCreatureReady(player1, new ThrullChampion());
        Permanent target = addCreatureReady(player2, new BasalThrull());

        activate(champion, target);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(champion.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability cannot target a non-Thrull")
    void cannotTargetNonThrull() {
        Permanent champion = addCreatureReady(player1, new ThrullChampion());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        int championIndex = gd.playerBattlefields.get(player1.getId()).indexOf(champion);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, championIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Thrull");
    }

    @Test
    @DisplayName("Control returns when Thrull Champion leaves the battlefield")
    void controlReturnsWhenSourceLeaves() {
        Permanent champion = addCreatureReady(player1, new ThrullChampion());
        Permanent target = addCreatureReady(player2, new BasalThrull());

        activate(champion, target);
        gd.playerBattlefields.get(player1.getId()).remove(champion);
        advanceToNextTurn(player1);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    private void activate(Permanent champion, Permanent target) {
        int championIndex = gd.playerBattlefields.get(player1.getId()).indexOf(champion);
        harness.activateAbility(player1, championIndex, null, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
