package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CrenellatedWall;
import com.github.laxika.magicalvibes.cards.i.IronLance;
import com.github.laxika.magicalvibes.cards.s.SilvergladeElemental;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Warpath.class, CrenellatedWall.class, IronLance.class, SilvergladeElemental.class})
class WarpathTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to each blocked creature and each blocking creature")
    void damagesBlockedAndBlockingCreatures() {
        Permanent blocked = addCreatureReady(player1, new SilvergladeElemental());

        Permanent blocker = addCreatureReady(player2, new CrenellatedWall());

        Permanent unblocked = addCreatureReady(player1, new SilvergladeElemental());

        Permanent idle = addCreatureReady(player2, new CrenellatedWall());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        castWarpath();

        assertThat(blocked.getMarkedDamage()).isEqualTo(3);
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        assertThat(unblocked.getMarkedDamage()).isZero();
        assertThat(idle.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not damage noncreature permanents")
    void doesNotDamageNoncreaturePermanents() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new IronLance());
        noncreature.setBlocking(true);

        castWarpath();

        assertThat(noncreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Deals no damage when there are no blocked or blocking creatures")
    void doesNotDamageCreaturesOutsideCombat() {
        Permanent creature = addCreatureReady(player1, new CrenellatedWall());

        castWarpath();

        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Still damages a blocked attacker after its last blocker leaves")
    void damagesBlockedAttackerAfterBlockerLeaves() {
        Permanent attacker = addCreatureReady(player1, new SilvergladeElemental());
        Permanent blocker = addCreatureReady(player2, new CrenellatedWall());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        harness.castFromHand(player1, new Warpath(), "{3}{R}");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, blocker));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
        harness.assertNotOnBattlefield(player2, "Crenellated Wall");
        harness.assertInHand(player2, "Crenellated Wall");
    }

    @Test
    @DisplayName("Damages every blocker but damages a multiply blocked attacker only once")
    void damagesMultipleBlockersAndAttackerOnce() {
        Permanent attacker = addCreatureReady(player1, new SilvergladeElemental());
        Permanent firstBlocker = addCreatureReady(player2, new CrenellatedWall());
        Permanent secondBlocker = addCreatureReady(player2, new CrenellatedWall());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(
                        new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        castWarpath();

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(3);
        assertThat(secondBlocker.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Lethal damage kills both blocked and blocking creatures")
    void lethalDamageKillsBothCombatants() {
        Permanent attacker = addCreatureReady(player1, new SilvergladeElemental());
        Permanent blocker = addCreatureReady(player2, new CrenellatedWall());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        attacker.setMarkedDamage(1);
        blocker.setMarkedDamage(1);

        castWarpath();

        harness.assertNotOnBattlefield(player1, "Silverglade Elemental");
        harness.assertInGraveyard(player1, "Silverglade Elemental");
        harness.assertNotOnBattlefield(player2, "Crenellated Wall");
        harness.assertInGraveyard(player2, "Crenellated Wall");
    }

    private void castWarpath() {
        harness.castFromHand(player1, new Warpath(), "{3}{R}");
        harness.passBothPriorities();
    }
}
