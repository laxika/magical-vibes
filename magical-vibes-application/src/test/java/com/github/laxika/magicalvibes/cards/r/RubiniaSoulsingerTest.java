package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RubiniaSoulsingerTest extends BaseCardTest {

    @Test
    @DisplayName("{T} gains control of a target creature while Rubinia remains tapped")
    void gainsControlWhileTapped() {
        Permanent rubinia = addReadyRubinia(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(rubinia);
        harness.activateAbility(player1, idx, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(rubinia.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent rubinia = addReadyRubinia(player1);
        Permanent land = new Permanent(new Island());
        gd.playerBattlefields.get(player2.getId()).add(land);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(rubinia);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Control ends when Rubinia untaps")
    void controlEndsWhenRubiniaUntaps() {
        Permanent rubinia = addReadyRubinia(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(rubinia);
        harness.activateAbility(player1, idx, null, bears.getId());
        harness.passBothPriorities();

        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(rubinia.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Choosing not to untap Rubinia retains control")
    void keepingRubiniaTappedRetainsControl() {
        Permanent rubinia = addReadyRubinia(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(rubinia);
        harness.activateAbility(player1, idx, null, bears.getId());
        harness.passBothPriorities();

        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(rubinia.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
    }

    private Permanent addReadyRubinia(Player player) {
        Permanent perm = new Permanent(new RubiniaSoulsinger());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
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

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
