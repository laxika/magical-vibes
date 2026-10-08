package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfTorches.class, RuneclawBear.class})
class WallOfTorchesTest extends BaseCardTest {

    @Test
    void defenderPreventsAttacking() {
        Permanent wall = addCreatureReady(player1, new WallOfTorches());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wall.isAttacking()).isFalse();
        assertThat(wall.isTapped()).isFalse();
    }

    @Test
    void canBlockAndDealsCombatDamageNormally() {
        addCreatureReady(player1, new RuneclawBear());
        harness.addToBattlefield(player2, new WallOfTorches());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Wall of Torches");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Wall of Torches");
        harness.assertLife(player2, 20);
    }
}
