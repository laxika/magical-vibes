package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GraazUnstoppableJuggernautTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control become 5/3 Juggernauts")
    void otherCreaturesBecomeFiveThreeJuggernauts() {
        addCreatureReady(player1, new GraazUnstoppableJuggernaut());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.computeStaticBonus(gd, ownCreature).grantedSubtypes())
                .contains(CardSubtype.JUGGERNAUT);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.computeStaticBonus(gd, opponentCreature).grantedSubtypes())
                .doesNotContain(CardSubtype.JUGGERNAUT);
    }

    @Test
    @DisplayName("Juggernauts you control must attack each combat if able")
    void juggernautsYouControlMustAttack() {
        addCreatureReady(player1, new GraazUnstoppableJuggernaut());
        addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Juggernauts you control can't be blocked by Walls")
    void juggernautsYouControlCannotBeBlockedByWalls() {
        addCreatureReady(player1, new GraazUnstoppableJuggernaut());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new WallOfWood());
        Permanent nonWall = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int wallIndex = gd.playerBattlefields.get(player2.getId()).indexOf(wall);
        int nonWallIndex = gd.playerBattlefields.get(player2.getId()).indexOf(nonWall);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(wallIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(nonWallIndex, attackerIndex)));
        assertThat(nonWall.isBlocking()).isTrue();
    }
}
