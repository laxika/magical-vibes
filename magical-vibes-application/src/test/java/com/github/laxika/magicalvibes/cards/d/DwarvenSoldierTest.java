package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DwarvenSoldierTest extends BaseCardTest {

    @Test
    @DisplayName("When Dwarven Soldier becomes blocked by an Orc it gets +0/+2 until end of turn")
    void becomesBlockedByOrcBoosts() {
        Permanent soldier = addReadySoldier(player1);
        soldier.setAttacking(true);
        addReadyCreature(player2, true); // Orc blocker

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(soldier.getPowerModifier()).isZero();
        assertThat(soldier.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Dwarven Soldier becomes blocked by a non-Orc it gets no boost")
    void becomesBlockedByNonOrcDoesNothing() {
        Permanent soldier = addReadySoldier(player1);
        soldier.setAttacking(true);
        addReadyCreature(player2, false); // non-Orc blocker

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(soldier.getPowerModifier()).isZero();
        assertThat(soldier.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("When Dwarven Soldier blocks an Orc it gets +0/+2 until end of turn")
    void blocksOrcBoosts() {
        Permanent attacker = addReadyCreature(player1, true); // Orc attacker
        attacker.setAttacking(true);
        Permanent soldier = addReadySoldier(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(soldier.getPowerModifier()).isZero();
        assertThat(soldier.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Dwarven Soldier blocks a non-Orc it gets no boost")
    void blocksNonOrcDoesNothing() {
        Permanent attacker = addReadyCreature(player1, false); // non-Orc attacker
        attacker.setAttacking(true);
        Permanent soldier = addReadySoldier(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(soldier.getPowerModifier()).isZero();
        assertThat(soldier.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Blocked by an Orc and a non-Orc, Dwarven Soldier gets +0/+2 only once")
    void becomesBlockedByOrcAndNonOrcBoostsOnce() {
        Permanent soldier = addReadySoldier(player1);
        soldier.setAttacking(true);
        addReadyCreature(player2, true);  // Orc blocker
        addReadyCreature(player2, false); // non-Orc blocker

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(soldier.getToughnessModifier()).isEqualTo(2);
    }

    private Permanent addReadySoldier(Player player) {
        Permanent permanent = new Permanent(new DwarvenSoldier());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature(Player player, boolean orc) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        if (orc) {
            permanent.getGrantedSubtypes().add(CardSubtype.ORC);
        }
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
