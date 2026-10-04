package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.b.BlazeOfGlory;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElderLandWurm.class, GiantSpider.class, BlazeOfGlory.class})
class ElderLandWurmTest extends BaseCardTest {

    @Test
    @DisplayName("When Elder Land Wurm blocks, it loses defender")
    void blockingRemovesDefender() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent wurm = addCreatureReady(player2, new ElderLandWurm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.DEFENDER)).isTrue();

        // Resolve the block trigger.
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wurm, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent wurm = addCreatureReady(player1, new ElderLandWurm());
        Permanent initialAttacker = addCreatureReady(player2, new GiantSpider());
        initialAttacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        harness.passUntil(TurnStep.CLEANUP);
        harness.performUntapStep(player1);

        Permanent spider = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                spider.getId(), 4,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(spider);
    }

    @Test
    @DisplayName("The defender loss lasts past end of turn (it can attack on a later turn)")
    void defenderLossPersistsPastEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent wurm = addCreatureReady(player2, new ElderLandWurm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wurm, Keyword.DEFENDER)).isFalse();
        assertThat(als.canAttack(gd, wurm, player2.getId())).isTrue();

        harness.passUntil(TurnStep.CLEANUP);

        // Unlike an "until end of turn" removal, the loss is indefinite.
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.DEFENDER)).isFalse();
        assertThat(als.canAttack(gd, wurm, player2.getId())).isTrue();
    }

    @Test
    @DisplayName("Elder Land Wurm does not trigger when it does not block")
    void doesNotTriggerWhenItDoesNotBlock() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent wurm = addCreatureReady(player2, new ElderLandWurm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.DEFENDER)).isTrue();
        assertThat(als.canAttack(gd, wurm, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Blocking multiple creatures triggers defender loss only once")
    void blockingMultipleCreaturesTriggersOnlyOnce() {
        addCreatureReady(player1, new GiantSpider());
        addCreatureReady(player1, new GiantSpider());
        Permanent wurm = addCreatureReady(player2, new ElderLandWurm());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0, 1)));
        harness.setHand(player1, List.of(new BlazeOfGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, wurm.getId());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.DEFENDER)).isFalse();
    }
}
