package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShanodinDryads.class, Forest.class, GrizzlyBears.class})
class ShanodinDryadsTest extends BaseCardTest {

    @Test
    @DisplayName("Shanodin Dryads cannot be blocked when defending player controls a Forest")
    void cannotBeBlockedWhenDefenderControlsForest() {
        harness.addToBattlefield(player2, new Forest());

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        Permanent atkPerm = addCreatureReady(player1, new ShanodinDryads());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Shanodin Dryads can be blocked when defending player does not control a Forest")
    void canBeBlockedWhenDefenderDoesNotControlForest() {
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        Permanent atkPerm = addCreatureReady(player1, new ShanodinDryads());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Shanodin Dryads can be blocked when only the attacking player controls a Forest")
    void canBeBlockedWhenOnlyAttackerControlsForest() {
        harness.addToBattlefield(player1, new Forest());
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        Permanent atkPerm = addCreatureReady(player1, new ShanodinDryads());

        declareAttackers(List.of(1));
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
