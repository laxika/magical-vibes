package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShuEliteInfantry.class, ForestBear.class})
class ShuEliteInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("Shu Elite Infantry can attack alone")
    void canAttackAlone() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new ShuEliteInfantry());
        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Shu Elite Infantry can attack with another creature")
    void canAttackWithAnother() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new ShuEliteInfantry());
        addCreatureReady(player1, new ForestBear());
        declareAttackers(List.of(0, 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Shu Elite Infantry can block alone")
    void canBlockAlone() {
        Permanent attacker = addCreatureReady(player1, new ForestBear());
        attacker.setAttacking(true);
        Permanent infantry = addCreatureReady(player2, new ShuEliteInfantry());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(infantry.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Shu Elite Infantry can block with another creature")
    void canBlockWithAnother() {
        Permanent attacker1 = addCreatureReady(player1, new ForestBear());
        attacker1.setAttacking(true);

        Permanent attacker2 = addCreatureReady(player1, new ForestBear());
        attacker2.setAttacking(true);

        Permanent infantry = addCreatureReady(player2, new ShuEliteInfantry());
        Permanent bears = addCreatureReady(player2, new ForestBear());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));

        assertThat(infantry.isBlocking()).isTrue();
        assertThat(bears.isBlocking()).isTrue();
    }
}
