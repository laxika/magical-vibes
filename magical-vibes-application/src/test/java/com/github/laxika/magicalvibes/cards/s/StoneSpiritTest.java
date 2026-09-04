package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.k.KjeldoranSkycaptain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StoneSpirit.class, BalduvianBears.class, KjeldoranSkycaptain.class})
class StoneSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Stone Spirit can't be blocked by a creature with flying")
    void cannotBeBlockedByFlyingCreature() {
        Permanent blockerPerm = addCreatureReady(player2, new KjeldoranSkycaptain());

        Permanent atkPerm = addCreatureReady(player1, new StoneSpirit());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Stone Spirit can be blocked by a creature without flying")
    void canBeBlockedByNonFlyingCreature() {
        Permanent blockerPerm = addCreatureReady(player2, new BalduvianBears());

        Permanent atkPerm = addCreatureReady(player1, new StoneSpirit());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Stone Spirit can be blocked by a non-flying creature among flying blockers")
    void canBeBlockedByNonFlyingCreatureAmongFlyingBlockers() {
        Permanent flyingBlocker = addCreatureReady(player2, new KjeldoranSkycaptain());
        Permanent nonFlyingBlocker = addCreatureReady(player2, new BalduvianBears());

        Permanent atkPerm = addCreatureReady(player1, new StoneSpirit());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int nonFlyingBlockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(nonFlyingBlocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(nonFlyingBlockerIdx, attackerIdx)));

        assertThat(nonFlyingBlocker.isBlocking()).isTrue();
        assertThat(flyingBlocker.isBlocking()).isFalse();
    }
}
