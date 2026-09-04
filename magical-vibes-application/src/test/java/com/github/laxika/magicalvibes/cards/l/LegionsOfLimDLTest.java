package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.d.DeepFreeze;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredSwamp;
import com.github.laxika.magicalvibes.cards.s.StaffOfTheAges;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LegionsOfLimDL.class, BalduvianBears.class, Swamp.class, SnowCoveredSwamp.class,
        SnowCoveredForest.class})
class LegionsOfLimDLTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked when defending player controls a snow Swamp")
    void cantBeBlockedWhenDefenderControlsSnowSwamp() {
        addSnowSwamp(player2);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        Permanent legions = readyAttacker(player1);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(legions)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Can be blocked when defending player controls only a non-snow Swamp")
    void canBeBlockedWithNonSnowSwamp() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        Permanent legions = readyAttacker(player1);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(legions))));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can be blocked when defending player controls only a snow Forest")
    void canBeBlockedWithSnowForest() {
        addSnowForest(player2);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        Permanent legions = readyAttacker(player1);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(legions))));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can be blocked when only the attacking player controls a snow Swamp")
    void canBeBlockedWhenOnlyAttackerControlsSnowSwamp() {
        addSnowSwamp(player1);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        Permanent legions = readyAttacker(player1);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(legions))));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @CardUsed(StaffOfTheAges.class)
    @DisplayName("Can be blocked when Staff of the Ages ignores snow swampwalk")
    void canBeBlockedWhenStaffIgnoresSnowSwampwalk() {
        addSnowSwamp(player2);
        harness.addToBattlefield(player2, new StaffOfTheAges());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        Permanent legions = readyAttacker(player1);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(legions))));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @CardUsed(DeepFreeze.class)
    @DisplayName("Can be blocked after Deep Freeze removes snow swampwalk")
    void canBeBlockedAfterLosingSnowSwampwalk() {
        addSnowSwamp(player2);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        Permanent legions = readyAttacker(player1);

        Permanent deepFreeze = new Permanent(new DeepFreeze());
        deepFreeze.setAttachedTo(legions.getId());
        gd.playerBattlefields.get(player2.getId()).add(deepFreeze);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(legions))));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Unblocked Legions deals 2 damage")
    void dealsTwoWhenUnblocked() {
        readyAttacker(player1);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent readyAttacker(Player player) {
        Permanent perm = addCreatureReady(player, new LegionsOfLimDL());
        perm.setAttacking(true);
        return perm;
    }

    private void addSnowSwamp(Player player) {
        harness.addToBattlefield(player, new SnowCoveredSwamp());
    }

    private void addSnowForest(Player player) {
        harness.addToBattlefield(player, new SnowCoveredForest());
    }

}
