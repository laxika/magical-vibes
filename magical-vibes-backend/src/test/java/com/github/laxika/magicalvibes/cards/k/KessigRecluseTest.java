package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class KessigRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Can block a flying creature due to reach")
    void canBlockFlyingCreature() {
        Permanent recluse = addCreatureReady(player2, new KessigRecluse());
        Permanent flyer = addAttackingFlyer();

        setDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(recluse);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(flyer);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deathtouch kills a larger creature it damages in combat")
    void deathtouchKillsLargerCreature() {
        Permanent recluse = addCreatureReady(player2, new KessigRecluse());
        Permanent flyer = addAttackingFlyer();

        setDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(recluse);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(flyer);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(flyer.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(recluse.getId()));
    }

    private Permanent addAttackingFlyer() {
        Permanent flyer = addCreatureReady(player1, new AirElemental());
        flyer.setAttacking(true);
        return flyer;
    }

    private void setDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
