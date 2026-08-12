package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrossGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Swamps reduces the generic mana cost")
    void affinityForSwampsReducesGenericCost() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Swamp());
        }
        harness.setHand(player1, List.of(new DrossGolem()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Affinity counts only Swamps controlled by the spell's controller")
    void affinityCountsOnlyControlledSwamps() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new Swamp());
        }
        harness.setHand(player1, List.of(new DrossGolem()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Fear stops a nonblack, nonartifact creature from blocking Dross Golem")
    void fearStopsNonblackNonartifactCreature() {
        Permanent golem = attackingGolem();
        gd.playerBattlefields.get(player1.getId()).add(golem);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Dross Golem (fear)");
    }

    @Test
    @DisplayName("Fear allows an artifact creature to block Dross Golem")
    void fearAllowsArtifactCreatureToBlock() {
        Permanent golem = attackingGolem();
        gd.playerBattlefields.get(player1.getId()).add(golem);

        Permanent ornithopter = new Permanent(new Ornithopter());
        ornithopter.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ornithopter);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    private Permanent attackingGolem() {
        Permanent golem = new Permanent(new DrossGolem());
        golem.setSummoningSick(false);
        golem.setAttacking(true);
        return golem;
    }
}
