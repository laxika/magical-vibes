package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({SabertoothAlleyCat.class, GrizzlyBears.class, WallOfAir.class})
class SabertoothAlleyCatTest extends BaseCardTest {

    @Test
    @DisplayName("Attacks each combat if able")
    void mustAttackEachCombat() {
        addCreatureReady(player1, new SabertoothAlleyCat());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Its ability prevents creatures without defender from blocking it")
    void nonDefenderCannotBlockAfterActivation() {
        Permanent cat = addCreatureReady(player1, new SabertoothAlleyCat());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        activateBlockingRestriction();
        cat.setAttacking(true);
        beginBlockers();

        assertThatThrownBy(() -> declareBlock(bears, cat))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with defender");
    }

    @Test
    @DisplayName("Its ability still allows creatures with defender to block it")
    void defenderCanBlockAfterActivation() {
        Permanent cat = addCreatureReady(player1, new SabertoothAlleyCat());
        Permanent wall = addCreatureReady(player2, new WallOfAir());
        activateBlockingRestriction();
        cat.setAttacking(true);
        beginBlockers();

        declareBlock(wall, cat);

        assertThat(wall.isBlocking()).isTrue();
    }

    private void activateBlockingRestriction() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void beginBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
