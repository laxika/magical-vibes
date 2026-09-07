package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PygmyAllosaurus.class, BalduvianBears.class, Swamp.class})
class PygmyAllosaurusTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked while the defending player controls a Swamp")
    void cannotBeBlockedWhenDefenderControlsSwamp() {
        Permanent attacker = addCreatureReady(player1, new PygmyAllosaurus());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        harness.addToBattlefield(player2, new Swamp());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Can be blocked while the defending player controls no Swamp")
    void canBeBlockedWhenDefenderControlsNoSwamp() {
        Permanent attacker = addCreatureReady(player1, new PygmyAllosaurus());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can be blocked when only the attacking player controls a Swamp")
    void attackersSwampDoesNotEnableSwampwalk() {
        Permanent attacker = addCreatureReady(player1, new PygmyAllosaurus());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        harness.addToBattlefield(player1, new Swamp());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
