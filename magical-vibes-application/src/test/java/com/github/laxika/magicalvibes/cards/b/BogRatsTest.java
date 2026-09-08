package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GlacialWall;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BogRats.class, GlacialWall.class, GrizzlyBears.class})
class BogRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Bog Rats can't be blocked by a Wall")
    void cannotBeBlockedByWall() {
        Permanent blockerPerm = addCreatureReady(player2, new GlacialWall());

        Permanent atkPerm = addCreatureReady(player1, new BogRats());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bog Rats can be blocked by a non-Wall creature")
    void canBeBlockedByNonWall() {
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        Permanent atkPerm = addCreatureReady(player1, new BogRats());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
