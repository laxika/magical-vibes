package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.n.NorwoodWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreeMonkey.class, AirElemental.class, NorwoodWarrior.class})
class TreeMonkeyTest extends BaseCardTest {

    @Test
    @DisplayName("Tree Monkey can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new AirElemental());
        attacker.setAttacking(true);
        Permanent treeMonkey = addCreatureReady(player2, new TreeMonkey());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(treeMonkey.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature without reach cannot block a creature with flying")
    void creatureWithoutReachCannotBlockFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new AirElemental());
        attacker.setAttacking(true);
        addCreatureReady(player2, new NorwoodWarrior());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
