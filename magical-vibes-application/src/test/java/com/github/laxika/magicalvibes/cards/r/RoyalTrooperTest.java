package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoyalTrooperTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking triggers +2/+2 until end of turn")
    void blockingTriggersBoost() {
        Permanent trooper = addReadyTrooper(player2);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(trooper.getPowerModifier()).isEqualTo(2);
        assertThat(trooper.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Royal Trooper's blocking boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent trooper = addReadyTrooper(player2);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(trooper.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.CLEANUP);
        trooper.resetModifiers();

        assertThat(trooper.getPowerModifier()).isZero();
        assertThat(trooper.getToughnessModifier()).isZero();
    }

    private Permanent addReadyTrooper(Player player) {
        Permanent permanent = new Permanent(new RoyalTrooper());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addReadyAttacker(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }
}
