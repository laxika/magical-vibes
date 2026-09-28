package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed({MaritLage.class, GrizzlyBears.class})
class MaritLageTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Marit Lage")
    void flyingPreventsGroundBlocker() {
        Permanent maritLage = addCreatureReady(player1, new MaritLage());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(maritLage)));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(maritLage);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Indestructible lets Marit Lage survive lethal damage")
    void indestructibleSurvivesLethalDamage() {
        Permanent maritLage = addCreatureReady(player1, new MaritLage());
        maritLage.setMarkedDamage(20);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(maritLage);
    }
}
