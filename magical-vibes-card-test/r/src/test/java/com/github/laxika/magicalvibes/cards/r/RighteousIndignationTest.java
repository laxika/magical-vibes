package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.c.CateranBrute;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.g.GerrardsIrregulars;
import com.github.laxika.magicalvibes.cards.g.GiantCaterpillar;
import com.github.laxika.magicalvibes.cards.w.WallOfGlare;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        RighteousIndignation.class,
        CateranBrute.class,
        GerrardsIrregulars.class,
        GiantCaterpillar.class,
        FreshVolunteers.class,
        WallOfGlare.class
})
class RighteousIndignationTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a black creature gives the blocker +1/+1")
    void blackAttackerBoostsBlocker() {
        Permanent attacker = addCreatureReady(player1, new CateranBrute());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new RighteousIndignation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, blocker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, blocker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking a red creature gives the blocker +1/+1")
    void redAttackerBoostsBlocker() {
        Permanent attacker = addCreatureReady(player1, new GerrardsIrregulars());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new RighteousIndignation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, blocker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, blocker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking a nonblack, nonred creature does not boost the blocker")
    void otherColoredAttackerDoesNotBoostBlocker() {
        Permanent attacker = addCreatureReady(player1, new GiantCaterpillar());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new RighteousIndignation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(PermanentGrants.powerBoostOf(gd, blocker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, blocker)).isZero();
    }

    @Test
    @DisplayName("A blocker gets one boost for each black or red creature it blocks")
    void eachMatchingBlockedAttackerTriggersSeparately() {
        Permanent blackAttacker = addCreatureReady(player1, new CateranBrute());
        blackAttacker.setAttacking(true);
        Permanent redAttacker = addCreatureReady(player1, new GerrardsIrregulars());
        redAttacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new WallOfGlare());
        harness.addToBattlefield(player1, new RighteousIndignation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, blocker)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, blocker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GerrardsIrregulars());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new RighteousIndignation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, blocker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, blocker)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, blocker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, blocker)).isZero();
    }

    @Test
    @DisplayName("Each creature blocking the same black attacker gets its own boost")
    void multipleBlockersEachGetBoosted() {
        Permanent attacker = addCreatureReady(player1, new CateranBrute());
        attacker.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new FreshVolunteers());
        Permanent secondBlocker = addCreatureReady(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new RighteousIndignation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, firstBlocker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstBlocker)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, secondBlocker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, secondBlocker)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
    }

    @Test
    @DisplayName("Enchantments controlled by either player independently boost the blocker")
    void multipleEnchantmentsTriggerRegardlessOfController() {
        Permanent attacker = addCreatureReady(player1, new CateranBrute());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new RighteousIndignation());
        harness.addToBattlefield(player2, new RighteousIndignation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, blocker)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, blocker)).isEqualTo(2);
    }

    @Test
    @DisplayName("A pending block trigger still resolves after its enchantment leaves")
    void triggerResolvesAfterEnchantmentLeavesBattlefield() {
        Permanent attacker = addCreatureReady(player1, new CateranBrute());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new RighteousIndignation());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(
                findPermanent(player1, "Righteous Indignation"));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, blocker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, blocker)).isEqualTo(1);
    }
}
