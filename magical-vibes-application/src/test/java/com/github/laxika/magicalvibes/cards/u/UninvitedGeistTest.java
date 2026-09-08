package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UninvitedGeistTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms after dealing combat damage to a player")
    void transformsAfterCombatDamageToPlayer() {
        Permanent geist = addCreatureReady(player1, new UninvitedGeist());
        geist.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(geist.isTransformed()).isTrue();
        assertThat(geist.getCard()).isInstanceOf(UnimpededTrespasser.class);
    }

    @Test
    @DisplayName("Does not transform when combat damage is prevented by a blocker")
    void doesNotTransformWhenBlocked() {
        Permanent geist = addCreatureReady(player1, new UninvitedGeist());
        geist.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(geist))));
        harness.passBothPriorities();

        assertThat(geist.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("The transformed creature cannot be blocked")
    void transformedCreatureCannotBeBlocked() {
        Permanent trespasser = addCreatureReady(player1, new UnimpededTrespasser());
        trespasser.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(trespasser)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }
}
