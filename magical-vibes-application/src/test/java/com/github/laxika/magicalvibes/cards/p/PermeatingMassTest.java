package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PermeatingMassTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a creature makes it a copy of Permeating Mass")
    void combatDamageToCreatureMakesItACopy() {
        Permanent mass = addCreatureReady(player1, new PermeatingMass());
        mass.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(blocker.getCard().getName()).isEqualTo("Permeating Mass");
        assertThat(blocker.getCard().getPower()).isEqualTo(1);
        assertThat(blocker.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The trigger uses Permeating Mass's last-known characteristics if it dies in combat")
    void triggerUsesLastKnownSourceIfMassDies() {
        Permanent mass = addCreatureReady(player1, new PermeatingMass());
        mass.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mass);
        assertThat(blocker.getCard().getName()).isEqualTo("Permeating Mass");
    }

    @Test
    @DisplayName("Combat damage to a player does not trigger Permeating Mass")
    void combatDamageToPlayerDoesNotTrigger() {
        Permanent mass = addCreatureReady(player1, new PermeatingMass());
        mass.setAttacking(true);
        Permanent creature = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCard().getName()).isEqualTo("Giant Spider");
    }
}
