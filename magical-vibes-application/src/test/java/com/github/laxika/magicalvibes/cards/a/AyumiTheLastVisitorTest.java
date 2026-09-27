package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MichikoKondaTruthSeeker;
import com.github.laxika.magicalvibes.cards.m.MirenTheMoaningWell;
import com.github.laxika.magicalvibes.cards.s.StaffOfTheAges;
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

@CardUsed({AyumiTheLastVisitor.class, Forest.class, GrizzlyBears.class,
        MichikoKondaTruthSeeker.class, MirenTheMoaningWell.class})
class AyumiTheLastVisitorTest extends BaseCardTest {

    @Test
    @DisplayName("Ayumi can't be blocked when defending player controls a legendary land")
    void cannotBeBlockedWhenDefenderControlsLegendaryLand() {
        harness.addToBattlefield(player2, new MirenTheMoaningWell());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent ayumi = addReadyAttacker(player1);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, ayumi))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Ayumi can be blocked when defending player controls a nonlegendary land")
    void canBeBlockedWhenDefenderControlsNonlegendaryLand() {
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent ayumi = addReadyAttacker(player1);

        prepareDeclareBlockers();
        declareBlock(blocker, ayumi);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Ayumi can be blocked when defending player controls a legendary nonland")
    void canBeBlockedWhenDefenderControlsLegendaryNonland() {
        harness.addToBattlefield(player2, new MichikoKondaTruthSeeker());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent ayumi = addReadyAttacker(player1);

        prepareDeclareBlockers();
        declareBlock(blocker, ayumi);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Ayumi can be blocked when only the attacking player controls a legendary land")
    void canBeBlockedWhenOnlyAttackingPlayerControlsLegendaryLand() {
        harness.addToBattlefield(player1, new MirenTheMoaningWell());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent ayumi = addReadyAttacker(player1);

        prepareDeclareBlockers();
        declareBlock(blocker, ayumi);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @CardUsed(StaffOfTheAges.class)
    @DisplayName("Ayumi can be blocked when Staff of the Ages ignores landwalk")
    void canBeBlockedWhenStaffIgnoresLandwalk() {
        harness.addToBattlefield(player2, new MirenTheMoaningWell());
        harness.addToBattlefield(player2, new StaffOfTheAges());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent ayumi = addReadyAttacker(player1);

        prepareDeclareBlockers();
        declareBlock(blocker, ayumi);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyAttacker(Player player) {
        Permanent attacker = addCreatureReady(player, new AyumiTheLastVisitor());
        attacker.setAttacking(true);
        return attacker;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
