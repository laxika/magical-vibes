package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SnarespinnerTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature with flying gives Snarespinner +2/+0 until end of turn")
    void blocksFlyingCreatureBoosts() {
        Permanent attacker = addReadyCreature(player1, true);
        attacker.setAttacking(true);
        Permanent snarespinner = addReadySnarespinner(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(snarespinner.getPowerModifier()).isEqualTo(2);
        assertThat(snarespinner.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Blocking a creature without flying does not boost Snarespinner")
    void blocksNonFlyingCreatureDoesNothing() {
        Permanent attacker = addReadyCreature(player1, false);
        attacker.setAttacking(true);
        Permanent snarespinner = addReadySnarespinner(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(snarespinner.getPowerModifier()).isZero();
        assertThat(snarespinner.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Becoming blocked by a creature with flying does not trigger Snarespinner")
    void becomesBlockedDoesNothing() {
        Permanent snarespinner = addReadySnarespinner(player1);
        snarespinner.setAttacking(true);
        addReadyCreature(player2, true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(snarespinner.getPowerModifier()).isZero();
        assertThat(snarespinner.getToughnessModifier()).isZero();
    }

    private Permanent addReadySnarespinner(Player player) {
        Permanent permanent = new Permanent(new Snarespinner());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature(Player player, boolean flying) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        if (flying) {
            permanent.getGrantedKeywords().add(Keyword.FLYING);
        }
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
