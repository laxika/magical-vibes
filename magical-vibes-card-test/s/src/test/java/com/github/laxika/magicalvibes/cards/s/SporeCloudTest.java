package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RiverMerfolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SporeCloud.class, RiverMerfolk.class})
class SporeCloudTest extends BaseCardTest {

    @Test
    void tapsBlockingCreaturesPreventsCombatDamageAndLocksCombatCreatures() {
        Permanent attacker = addCreature(player2);
        attacker.setAttacking(true);

        Permanent blocker = addCreature(player2);
        blocker.setBlocking(true);

        Permanent uninvolved = addCreature(player2);

        castAndResolve();

        assertThat(attacker.isTapped()).isFalse();
        assertThat(blocker.isTapped()).isTrue();
        assertThat(uninvolved.isTapped()).isFalse();
        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
        assertThat(blocker.getSkipUntapCount()).isEqualTo(1);
        assertThat(uninvolved.getSkipUntapCount()).isZero();
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    void affectsAttackersAndBlockersControlledByDifferentPlayers() {
        Permanent attacker = addCreature(player1);
        Permanent blocker = addCreature(player2);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        castAndResolve(player2);

        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getSkipUntapCount()).isEqualTo(1);

        resolveCombat(player1);

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    void affectedCreaturesSkipTheirNextUntapStepOnly() {
        Permanent attacker = addCreature(player2);
        attacker.setAttacking(true);
        attacker.tap();

        Permanent blocker = addCreature(player2);
        blocker.setBlocking(true);

        castAndResolve();
        advanceToNextUpkeep(player2);

        assertThat(attacker.isTapped()).isTrue();
        assertThat(blocker.isTapped()).isTrue();

        advanceToNextUpkeep(player1);
        advanceToNextUpkeep(player2);

        assertThat(attacker.isTapped()).isFalse();
        assertThat(blocker.isTapped()).isFalse();
    }

    @Test
    void castingBeforeCombatPreventsDamageWithoutLockingLaterAttackers() {
        Permanent attacker = addCreature(player1);
        harness.setLife(player2, 20);

        castAndResolve();
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(attacker.getSkipUntapCount()).isZero();

        advanceToNextUpkeep(player2);
        assertThat(gd.preventAllCombatDamage).isFalse();
        advanceToNextUpkeep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    void eachCombatCreatureSkipsOnlyItsOwnControllersNextUntap() {
        Permanent attacker = addCreature(player1);
        Permanent blocker = addCreature(player2);
        Permanent uninvolved = addCreature(player2);
        uninvolved.tap();

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        castAndResolve(player2);
        resolveCombat(player1);

        advanceToNextUpkeep(player2);
        assertThat(blocker.isTapped()).isTrue();
        assertThat(uninvolved.isTapped()).isFalse();
        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);

        advanceToNextUpkeep(player1);
        assertThat(attacker.isTapped()).isTrue();

        advanceToNextUpkeep(player2);
        assertThat(blocker.isTapped()).isFalse();
        advanceToNextUpkeep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    void untappedAttackerConsumesRestrictionDuringItsNextUntapStep() {
        Permanent attacker = addCreature(player2);
        attacker.setAttacking(true);

        castAndResolve();
        advanceToNextUpkeep(player2);
        assertThat(attacker.isTapped()).isFalse();

        attacker.tap();
        advanceToNextUpkeep(player1);
        advanceToNextUpkeep(player2);
        assertThat(attacker.isTapped()).isFalse();
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new RiverMerfolk());
    }

    private void castAndResolve() {
        castAndResolve(player1);
    }

    private void castAndResolve(Player caster) {
        harness.setHand(caster, List.of(new SporeCloud()));
        harness.addMana(caster, ManaColor.GREEN, 3);
        harness.castInstant(caster, 0);
        harness.passBothPriorities();
    }

    private void advanceToNextUpkeep(Player activePlayer) {
        Player previousActivePlayer = activePlayer == player1 ? player2 : player1;
        harness.forceActivePlayer(previousActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.UPKEEP);
    }

}
