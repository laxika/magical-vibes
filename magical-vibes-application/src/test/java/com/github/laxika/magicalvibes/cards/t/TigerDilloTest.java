package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({TigerDillo.class, EnormousBaloth.class, GrizzlyBears.class})
class TigerDilloTest extends BaseCardTest {

    @Test
    @DisplayName("Tiger-Dillo cannot attack without another creature with power 4 or greater")
    void cannotAttackWithoutAnotherLargeCreature() {
        addCreatureReady(player1, new TigerDillo());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tiger-Dillo can attack when its controller controls another creature with power 4 or greater")
    void canAttackWithAnotherLargeCreature() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new TigerDillo());
        addCreatureReady(player1, new EnormousBaloth());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Tiger-Dillo cannot block without another creature with power 4 or greater")
    void cannotBlockWithoutAnotherLargeCreature() {
        Permanent tigerDillo = addCreatureReady(player2, new TigerDillo());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        prepareDeclareBlockers(attacker);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(tigerDillo);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tiger-Dillo can block when its controller controls another creature with power 4 or greater")
    void canBlockWithAnotherLargeCreature() {
        Permanent tigerDillo = addCreatureReady(player2, new TigerDillo());
        addCreatureReady(player2, new EnormousBaloth());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        prepareDeclareBlockers(attacker);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(tigerDillo);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(tigerDillo.isBlocking()).isTrue();
    }

    private void prepareDeclareBlockers(Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
