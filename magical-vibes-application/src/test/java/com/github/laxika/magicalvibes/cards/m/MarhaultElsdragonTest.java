package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarhaultElsdragonTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Marhault Elsdragon gets no rampage bonus")
    void oneBlockerGivesNoBonus() {
        Permanent marhault = addReadyMarhault(player1);
        marhault.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(marhault.getPowerModifier()).isZero();
        assertThat(marhault.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("With two blockers Marhault Elsdragon gets +1/+1 until end of turn")
    void twoBlockersGivePlusOne() {
        Permanent marhault = addReadyMarhault(player1);
        marhault.setAttacking(true);
        addReadyBears(player2);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(marhault.getPowerModifier()).isEqualTo(1);
        assertThat(marhault.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("With three blockers Marhault Elsdragon gets +2/+2 until end of turn")
    void threeBlockersGivePlusTwo() {
        Permanent marhault = addReadyMarhault(player1);
        marhault.setAttacking(true);
        addReadyBears(player2);
        addReadyBears(player2);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.passBothPriorities();

        assertThat(marhault.getPowerModifier()).isEqualTo(2);
        assertThat(marhault.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("If unblocked Marhault Elsdragon gets no rampage bonus")
    void unblockedGivesNoBonus() {
        Permanent marhault = addReadyMarhault(player1);
        marhault.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(marhault.getPowerModifier()).isZero();
        assertThat(marhault.getToughnessModifier()).isZero();
    }

    private Permanent addReadyMarhault(Player player) {
        Permanent permanent = new Permanent(new MarhaultElsdragon());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addReadyBears(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }
}
