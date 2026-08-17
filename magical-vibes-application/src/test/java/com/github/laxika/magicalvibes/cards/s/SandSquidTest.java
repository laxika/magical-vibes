package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SandSquidTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability taps the target creature")
    void resolvingAbilityTapsTargetCreature() {
        addReadySandSquid(player1);
        Permanent targetCreature = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(targetCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addReadySandSquid(player1);
        Permanent land = addReadyLand(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The target creature remains tapped while Sand Squid remains tapped")
    void targetCreatureDoesNotUntapWhileSandSquidRemainsTapped() {
        Permanent sandSquid = addReadySandSquid(player1);
        Permanent targetCreature = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();
        advanceToNextTurn(player1);

        assertThat(sandSquid.isTapped()).isTrue();
        assertThat(targetCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The target creature untaps after Sand Squid untaps")
    void targetCreatureUntapsAfterSandSquidUntaps() {
        Permanent sandSquid = addReadySandSquid(player1);
        Permanent targetCreature = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, true);
        advanceToNextTurn(player1);

        assertThat(sandSquid.isTapped()).isFalse();
        assertThat(targetCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The controller may choose not to untap Sand Squid")
    void mayChooseNotToUntap() {
        Permanent sandSquid = addReadySandSquid(player1);
        sandSquid.tap();

        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(sandSquid.isTapped()).isTrue();
    }

    private Permanent addReadySandSquid(Player player) {
        Permanent permanent = new Permanent(new SandSquid());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyLand(Player player) {
        Permanent permanent = new Permanent(new Forest());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
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
