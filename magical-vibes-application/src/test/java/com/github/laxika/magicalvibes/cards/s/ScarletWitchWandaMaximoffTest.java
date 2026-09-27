package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScarletWitchWandaMaximoff.class, GrizzlyBears.class})
class ScarletWitchWandaMaximoffTest extends BaseCardTest {

    @Test
    void menaceCannotBeBlockedByOnlyOneCreature() {
        Permanent attacker = addCreatureReady(player1, new ScarletWitchWandaMaximoff());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    void menaceCanBeBlockedByTwoCreatures() {
        Permanent attacker = addCreatureReady(player1, new ScarletWitchWandaMaximoff());
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int firstBlockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(firstBlocker);
        int secondBlockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(secondBlocker);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(firstBlockerIndex, attackerIndex),
                new BlockerAssignment(secondBlockerIndex, attackerIndex)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
