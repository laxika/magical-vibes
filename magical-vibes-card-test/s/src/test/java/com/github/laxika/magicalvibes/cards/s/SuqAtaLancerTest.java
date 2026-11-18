package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GhostShip;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuqAtaLancer.class, GhostShip.class})
class SuqAtaLancerTest extends BaseCardTest {

    @Test
    @DisplayName("Flanking gives a blocker without flanking -1/-1 until end of turn")
    void flankingHitsNonFlankingBlocker() {
        Permanent lancer = addCreatureReady(player1, new SuqAtaLancer());
        lancer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GhostShip());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Flanking does not weaken a blocker that has flanking")
    void flankingDoesNotAffectFlankingBlocker() {
        Permanent lancer = addCreatureReady(player1, new SuqAtaLancer());
        lancer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SuqAtaLancer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flanking's -1/-1 effect wears off at end of turn")
    void flankingPenaltyWearsOffAtEndOfTurn() {
        Permanent lancer = addCreatureReady(player1, new SuqAtaLancer());
        lancer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GhostShip());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Flanking weakens every non-flanking blocker independently")
    void flankingAffectsEachNonFlankingBlocker() {
        Permanent lancer = addCreatureReady(player1, new SuqAtaLancer());
        lancer.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new GhostShip());
        Permanent secondBlocker = addCreatureReady(player2, new GhostShip());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, firstBlocker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, firstBlocker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondBlocker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, secondBlocker)).isEqualTo(3);
    }

    @Test
    @DisplayName("A blocker with flanking does not exempt other blockers from flanking")
    void flankingChecksEachBlockersOwnAbility() {
        Permanent lancer = addCreatureReady(player1, new SuqAtaLancer());
        lancer.setAttacking(true);
        Permanent flankingBlocker = addCreatureReady(player2, new SuqAtaLancer());
        Permanent otherBlocker = addCreatureReady(player2, new GhostShip());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, flankingBlocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, flankingBlocker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherBlocker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otherBlocker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Haste lets Suq'Ata Lancer attack the turn it enters")
    void hasteLetsItAttackWhileSummoningSick() {
        harness.setLife(player2, 20);

        harness.addToBattlefieldAndReturn(player1, new SuqAtaLancer());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
