package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IvyDancer.class, Forest.class})
class IvyDancerTest extends BaseCardTest {

    @Test
    @DisplayName("The ability grants forestwalk to target creature")
    void abilityGrantsForestwalk() {
        addReadyIvyDancer(player1);
        Permanent target = addReadyIvyDancer(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Granted forestwalk is removed at end of turn")
    void forestwalkRemovedAtEndOfTurn() {
        addReadyIvyDancer(player1);
        Permanent target = addReadyIvyDancer(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Forestwalk prevents blocking while the defending player controls a Forest")
    void forestwalkPreventsBlockingWithForest() {
        addReadyIvyDancer(player1);
        Permanent attacker = addReadyIvyDancer(player1);
        Permanent blocker = addReadyIvyDancer(player2);
        harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FORESTWALK)).isTrue();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Forestwalk allows blocking while the defending player controls no Forest")
    void forestwalkAllowsBlockingWithoutForest() {
        addReadyIvyDancer(player1);
        Permanent attacker = addReadyIvyDancer(player1);
        Permanent blocker = addReadyIvyDancer(player2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FORESTWALK)).isTrue();
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyIvyDancer(player1);
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ivy Dancer can target itself and taps immediately as a cost")
    void canTargetItselfAndPaysTapCost() {
        Permanent dancer = addReadyIvyDancer(player1);

        harness.activateAbility(player1, 0, null, dancer.getId());

        assertThat(dancer.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, dancer, Keyword.FORESTWALK)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dancer, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("A tapped Ivy Dancer cannot activate its ability")
    void cannotActivateWhileTapped() {
        Permanent dancer = addReadyIvyDancer(player1);
        dancer.tap();
        Permanent target = addReadyIvyDancer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("An Ivy Dancer with summoning sickness cannot activate its ability")
    void cannotActivateWithSummoningSickness() {
        Permanent dancer = addReadyIvyDancer(player1);
        dancer.setSummoningSick(true);
        Permanent target = addReadyIvyDancer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(dancer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Forest controlled by the attacker does not prevent blocking")
    void attackersForestDoesNotPreventBlocking() {
        addReadyIvyDancer(player1);
        Permanent attacker = addReadyIvyDancer(player1);
        Permanent blocker = addReadyIvyDancer(player2);
        harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FORESTWALK)).isTrue();
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyIvyDancer(Player player) {
        return addCreatureReady(player, new IvyDancer());
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
