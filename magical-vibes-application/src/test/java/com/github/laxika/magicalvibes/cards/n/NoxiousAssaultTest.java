package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoxiousAssaultTest extends BaseCardTest {

    @Test
    void boostsOnlyCreaturesControlledByTheCaster() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castNoxiousAssault();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(2);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(2);
        assertThat(opponentCreature.getPowerModifier()).isZero();
        assertThat(opponentCreature.getToughnessModifier()).isZero();
    }

    @Test
    void givesTheControllerOfEachBlockingCreatureApoisonCounter() {
        Permanent attackerOne = addReady(player1);
        attackerOne.setAttacking(true);
        Permanent attackerTwo = addReady(player1);
        attackerTwo.setAttacking(true);
        addReady(player2);
        addReady(player2);

        castNoxiousAssault();

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void noCreatureBlockingMeansNoPoisonCounter() {
        Permanent attacker = addReady(player1);
        attacker.setAttacking(true);
        addReady(player2);

        castNoxiousAssault();

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    private void castNoxiousAssault() {
        harness.setHand(player1, List.of(new NoxiousAssault()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
