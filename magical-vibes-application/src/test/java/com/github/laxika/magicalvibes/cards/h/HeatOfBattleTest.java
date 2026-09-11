package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.SkyshroudFalcon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeatOfBattle.class, SkyshroudFalcon.class, SpinedWurm.class, Shock.class})
class HeatOfBattleTest extends BaseCardTest {

    @Test
    @DisplayName("A blocking creature's controller is dealt 1 damage")
    void blockerControllerTakesDamage() {
        Permanent attacker = addCreatureReady(player1, new SpinedWurm());
        attacker.setAttacking(true);
        addCreatureReady(player2, new SpinedWurm());
        harness.addToBattlefield(player1, new HeatOfBattle());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each blocking creature triggers separately")
    void triggersOncePerBlocker() {
        Permanent attacker1 = addCreatureReady(player1, new SpinedWurm());
        attacker1.setAttacking(true);
        Permanent attacker2 = addCreatureReady(player1, new SpinedWurm());
        attacker2.setAttacking(true);
        addCreatureReady(player2, new SpinedWurm());
        addCreatureReady(player2, new SpinedWurm());
        harness.addToBattlefield(player1, new HeatOfBattle());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The trigger is symmetric when its controller's creature blocks")
    void ownControllerTakesDamageWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new SpinedWurm());
        attacker.setAttacking(true);
        addCreatureReady(player2, new SpinedWurm());
        harness.addToBattlefield(player2, new HeatOfBattle());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("No blockers means no Heat of Battle damage")
    void noBlockersNoHeatOfBattleDamage() {
        Permanent attacker = addCreatureReady(player1, new SpinedWurm());
        attacker.setAttacking(true);
        addCreatureReady(player2, new SpinedWurm());
        harness.addToBattlefield(player1, new HeatOfBattle());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("The trigger still damages the blocker's controller if the blocker leaves first")
    void triggerUsesBlockersLastKnownController() {
        Permanent attacker = addCreatureReady(player1, new SpinedWurm());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SkyshroudFalcon());
        harness.addToBattlefield(player1, new HeatOfBattle());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }
}
