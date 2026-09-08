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
    @DisplayName("{T} gains control of a target creature; Rubinia stays tapped")
    void gainsControlOfCreature() {
        Permanent rubinia = addReadyRubinia(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        activate(rubinia, creature);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(rubinia.isTapped()).isTrue();
        assertThat(gd.newestControlEffectFor(creature.getId()).sourcePermanentId()).isEqualTo(rubinia.getId());
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent rubinia = addReadyRubinia(player1);
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(rubinia);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Control is lost when Rubinia untaps during its controller's untap step")
    void controlLostWhenRubiniaUntaps() {
        Permanent rubinia = addReadyRubinia(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        activate(rubinia, creature);

        advanceToNextTurn(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(creature.getId()));

        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(rubinia.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.controlEffectsFor(creature.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing not to untap Rubinia retains control across the untap step")
    void keepingTappedRetainsControl() {
        Permanent rubinia = addReadyRubinia(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        activate(rubinia, creature);

        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(rubinia.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Control is lost when Rubinia leaves the battlefield")
    void controlLostWhenRubiniaLeaves() {
        Permanent rubinia = addReadyRubinia(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        activate(rubinia, creature);

        gd.playerBattlefields.get(player1.getId()).remove(rubinia);
        advanceToNextTurn(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(creature.getId()));
    }

    private Permanent addReadyRubinia(Player player) {
        Permanent perm = new Permanent(new RubiniaSoulsinger());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void activate(Permanent rubinia, Permanent target) {
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(rubinia);
        harness.activateAbility(player1, idx, null, target.getId());
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
