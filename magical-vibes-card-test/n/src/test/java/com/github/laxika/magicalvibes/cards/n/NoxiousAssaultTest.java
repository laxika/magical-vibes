package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoxiousAssault.class, GrizzlyBears.class, CopperLonglegs.class})
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

    @Test
    void blockingCreatureSacrificedBeforeTriggerResolvesStillPoisonsItsController() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new CopperLonglegs());
        castNoxiousAssault();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.assertInGraveyard(player2, "Copper Longlegs");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void creaturesEnteringAfterResolutionAreNotBoostedButStillTriggerWhenBlocking() {
        castNoxiousAssault();
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        attacker.setAttacking(true);

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void multipleResolutionsCreateIndependentBlockingTriggers() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new CopperLonglegs());
        castNoxiousAssault();
        castNoxiousAssault();

        assertThat(attacker.getPowerModifier()).isEqualTo(4);
        assertThat(attacker.getToughnessModifier()).isEqualTo(4);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    void boostAndBlockingTriggerExpireAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        harness.addToBattlefield(player2, new CopperLonglegs());
        castNoxiousAssault();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        creature.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    private void castNoxiousAssault() {
        harness.castFromHand(player1, new NoxiousAssault(), "{3}{G}{G}");
        harness.passBothPriorities();
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
